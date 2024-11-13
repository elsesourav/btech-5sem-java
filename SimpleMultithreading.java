public class SimpleMultithreading {
   private static void countNumbers(int start, int end, int step, String prefix) {
      try {
         for (int i = start; i != end; i += step) {
            System.out.println(prefix + i);
            Thread.sleep(500);
         }
      } catch (InterruptedException e) {
         System.out.println(prefix + "thread was interrupted.");
      }
   }

   public static void main(String[] args) {
      long startTime = System.currentTimeMillis();

      Thread countDown = new Thread(() -> countNumbers(10, 0, -1, "Count down: "));
      Thread countUp = new Thread(() -> countNumbers(1, 11, 1, "Count up: "));

      countDown.start();
      countUp.start();

      try {
         countDown.join();
         countUp.join();
      } catch (InterruptedException e) {
         System.out.println("Main thread was interrupted.");
      }

      long endTime = System.currentTimeMillis();
      double totalTimeSeconds = (endTime - startTime) / 1000.0;
      System.out.printf("Total execution time: %.2f seconds%n", totalTimeSeconds);
   }
}