package org.parallel.executors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Применение интерфейса Callable
 */
public class ExecutorDemo {
    /**
     * Подсчитывает количетсво вхождений заданного слова в указанном файле
     * @param word - слово для поиска
     * @param path - путь к файлу
     * @return
     */
    public static long occurrebnces(String word, Path path){
        try(var in = new Scanner(path)){
            int count = 0;
            while (in.hasNext()){
                if(in.next().equals(word)){
                    count++;
                }
            }
            return count;
        }
        catch (IOException e){
            return 0;
        }
    }

    /**
     * Возвращает все каталоги, порожденные заданным каталогом
     * @param rootDir корневой каталог
     * @return Возвращает множество каталогов, порожденных заданным каталогом
     */
    public static Set<Path> descendants(Path rootDir)
        throws IOException {
            try (Stream<Path> entries = Files.walk(rootDir)){
                return entries.filter(Files::isRegularFile).collect(Collectors.toSet());
            }
    }

    /**
     * Выдает задачу поиска слова в файле
     * @param word слово для поиска
     * @param path путь к файлу для поиска слова
     * @return Возвращает задачу поиска, выдающую путь при удачном завершении
     */
    public static Callable<Path> searchForTask(String word, Path path){
        return () -> {
            try(var in = new Scanner(path)){
                while(in.hasNext()){
                    if (in.next().equals(word)){
                        return path;
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("Search in " + path + " canceled.");
                        return null;
                    }
                }
                throw new NoSuchElementException();
            }
        };
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        try(var in = new Scanner(System.in)){

            System.out.println("Enter base directory: e.g. D:\\myprojects");
            String start = in.nextLine();
            System.out.println("Enter keyword: e.g. volatile");
            String word = in.nextLine();

            Set<Path> files = descendants(Path.of(start));
            var tasks = new ArrayList<Callable<Long>>();
            for(Path file : files){
                Callable<Long> task = () ->
                        occurrebnces(word, file);
                tasks.add(task);
            }
            ExecutorService executor = Executors.newCachedThreadPool();
            // использовать один исполнитель потоков вместо
            // того, чтобы выяснять,  можно ли ускорить поиск
            // с помощью нескольких потоков исполнения
            // ExecutorService executorService = Executors.newSingleThreadExecutor();
            Instant startTime = Instant.now();
            List<Future<Long>> results = executor.invokeAll(tasks);
            long total = 0;
            for (Future<Long> result : results){
                total += result.get();
            }
            Instant endTime = Instant.now();
            System.out.println("Occurencces of " + word + ": " + total);
            System.out.println("Time elapsed: " + Duration.between(startTime, endTime).toMillis() + " ms");

            var searchTasks = new ArrayList<Callable<Path>>();
            for(Path file :files){
                searchTasks.add(searchForTask(word, file));
            }
            Path found = executor.invokeAny(searchTasks);

            System.out.println(word + " occurs in: " + found);

            if (executor instanceof ThreadPoolExecutor){
                //не единственный исполнитель потоков
                System.out.println("Largest pool size: " +
                        ((ThreadPoolExecutor) executor).getLargestPoolSize());
                executor.shutdown();
            }


        }
    }
}

