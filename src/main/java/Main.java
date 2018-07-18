

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Scanner;

public class Main {

    private static final String OPEN = "open";
    private static final String CHECK_LINK_PRESENTED_BY_HREF = "checkLinkPresentByHref";
    private static final String CHECK_LINK_PRESENTED_BY_NAME = "checkLinkPresentByName";
    private static final String CHECK_PAGE_TITLE = "checkPageTitle";
    private static final String CHECK_PAGE_CONTAINS = "checkPageContains";

    // file path
    private static final String RESULT_FILE_PATH = "C:\\Users\\Kseniya\\IdeaProjects\\HT3\\result.txt";
    private static final String INPUT_FILE_PATH = "C:\\Users\\Kseniya\\IdeaProjects\\HT3\\input.txt";

    private static String sCurrentPage;
    private static boolean sIsCurrentCommandSuccess;

    public static void main(String[] args) {
        try {
            Scanner in = new Scanner(new FileReader(INPUT_FILE_PATH));
            int successCount = 0;
            int failureCount = 0;
            float totalTime = 0f;

            // for every line read from file
            while (in.hasNext()) {
                totalTime += executeCommand(in.nextLine());

                if (sIsCurrentCommandSuccess) successCount++;
                else failureCount++;
            }

            String formattedTotalTime = formatTime(totalTime);
            int totalCount = successCount + failureCount;
            String formattedAverageTime = formatTime(totalTime/ totalCount);

            String result = String.format(
                    "\nTotal tests: %d" +
                            "\nPassed/Failed %d/%d" +
                            "\nTotal time: %s" +
                            "\nAverage time: %s",
                    totalCount,
                    successCount, failureCount,
                    formattedTotalTime,
                    formattedAverageTime);

            writeFile(RESULT_FILE_PATH, result);
            in.close();

        } catch (FileNotFoundException e) {
            System.err.println("Can't find file, please try once again");
        }
    }

    private static float executeCommand(String line) {
        // separate line for command and arguments
        String[] lineParts = line.split(" ", 2);
        String command = lineParts[0];
        String argsPart = lineParts[1];

        // separate arguments by " symbol
        String[] args = argsPart.split("\" ");
        for (int i = 0; i < args.length; i++) {
            // remove all " " symbols
            args[i] = args[i].replaceAll("\"", "");
        }

        String firstArg = args[0];

        boolean commandSuccess;

        long startTime = System.nanoTime();

        switch (command) {
            case OPEN:
                int timeoutSeconds = Integer.parseInt(args[1]);
                int timeoutMillis = timeoutSeconds * 1000;
                commandSuccess = executeOpenCommand(timeoutMillis, firstArg);
                break;

            case CHECK_LINK_PRESENTED_BY_HREF:
                commandSuccess = checkLinkPresentedByHref(firstArg);
                break;
            case CHECK_LINK_PRESENTED_BY_NAME:
                commandSuccess = checkLinkPresentedByName(firstArg);
                break;
            case CHECK_PAGE_TITLE:
                commandSuccess = checkPageTitle(firstArg);
                break;
            case CHECK_PAGE_CONTAINS:
                commandSuccess = checkPageContains(firstArg);
                break;

            default:
                System.err.println("Unknown command = " + command + ",ignore it.");
                throw new IllegalArgumentException();
        }

        float executionTime = (System.nanoTime() - startTime) / 1_000_000_000f;
        String formattedTime = formatTime(executionTime);
        String commandResultChar = commandSuccess ? "+" : "-";
        String result = String.format("%s [%s] %s\n", commandResultChar, line, formattedTime);
        writeFile(RESULT_FILE_PATH, result);

        sIsCurrentCommandSuccess = commandSuccess;

        return executionTime;
    }

    private static void writeFile(String path, String content) {
        try {
            FileWriter fw = new FileWriter(path,true);
            fw.append(content);
            fw.close();
        } catch (Exception e) {
            System.err.print("Error during writing to the file");
        }
    }

    private static String formatTime(float time) {
        // format time to print only 3 value after . also don't print dot if value is int
        return new DecimalFormat("#.###").format(time);
    }

    private static boolean executeOpenCommand(int timeout, String pageUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(pageUrl).openConnection();
            connection.setReadTimeout(timeout);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            in.close();

            sCurrentPage = stringBuilder.toString();
            return true;

        } catch (Exception e) {
            // if any exception happened -> means that command is not successful
            sCurrentPage = "";
            return false;
        }
    }

    private static boolean checkPageContains(String text) {
        return sCurrentPage.contains(text);
    }

    private static boolean checkPageTitle(String title) {
        return sCurrentPage.contains("<title>" + title + "</title>");
    }

    private static boolean checkLinkPresentedByName(String name) {
        return sCurrentPage.contains(">" + name + "</a>");
    }

    private static boolean checkLinkPresentedByHref(String href) {
        return sCurrentPage.contains("href=\"" + href + "\"");
    }

}