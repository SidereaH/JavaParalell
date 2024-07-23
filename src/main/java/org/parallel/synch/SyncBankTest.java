package org.parallel.synch;

public class SyncBankTest {
    public static final  int NACCOUNTS = 100;
    public static final double INITIAL_BALANCE = 1000;
    public static final double MAX_AMOUNT = 100;
    public static int DELAY = 10;
    public static void main(String[] args) throws InterruptedException {
        var bank = new Bank(NACCOUNTS, INITIAL_BALANCE);
        for(int i = 0; i< NACCOUNTS; i++){
            int fromAccount =i;
            Runnable r = () ->{
                while(true){
                    int toAccount = (int) (bank.size() * Math.random());
                    double amount = MAX_AMOUNT * Math.random();
                    try {
                        bank.transfer(fromAccount, toAccount, amount);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        Thread.sleep((long) (DELAY * Math.random()));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            };
            new Thread(r).start();
        }

    }
}
