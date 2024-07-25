package org.parallel.threadsafecollections;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConcurrentHashMapTest {
    /**
     * Демонстрация применений параллеьных хеш-отображений
     */
    public static ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();

    /**
     * Вводит все слова из заданного файла в параллельное хеш-отображение
     * @param file - Заданный файл для поиска
     */
    public static void process(Path file) throws IOException {
        try(var in = new Scanner(file)){
            while(in.hasNext()){
                String word = in.next();
                map.merge(word, 1L, Long::sum);
            }
        }
    }

    public static Set<Path> descendants (Path rootDir) throws IOException{
        try(Stream<Path> entries = Files.walk(rootDir)){
            return entries.collect(Collectors.toSet());
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(processors);
        Path pathToRoot = Path.of(".");

        for (Path p : descendants(pathToRoot)) {
            if (p.getFileName().toString().endsWith(".java")) {
                executor.execute(() -> {
                    try {
                        process(p);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);

        map.forEach((k, v) -> {
            if (v >= 10) {
                System.out.println(k + " occurs " + v + " times");
            }
        });
    }
}
