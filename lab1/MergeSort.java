import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

public class MergeSort {
    public static void main(String[] args) throws IOException {
        int stepCnt = 22;
        int numCount = (int) Math.pow(2, stepCnt);
        int memoryLimitInKb = 5 * 1024;
        int inMemorySortSteps = (int) log2(memoryLimitInKb * 1024 / 4);

        String fileA = "A.txt";
        String fileB = "B.txt";
        String fileC = "C.txt";
        String fileG = "Generated.txt";
        generateNumber(fileG, numCount);
        Path path = Paths.get(fileG);
        long size = Files.size(path);
        System.out.println("Максимальный объем ОЗУ: " + memoryLimitInKb + " Kб");
        System.out.println("Размер сгенерированного файла: " + size / 1024 + " Kб");
        System.out.println("Количество чисел в файле: " + numCount);
        System.out.println("Общее количество шагов сортировки: " + stepCnt);
        System.out.println("Количество шагов сортировки в ОЗУ: " + inMemorySortSteps);
        System.out.println("Максимальное кол-во чисел которое можно поместить в буфер: " + (int) Math.pow(2, inMemorySortSteps));
        System.out.println("Объем памяти занимаемые этими числами: " + (int) Math.pow(2, inMemorySortSteps) * 4 / 1024 + " Kb");

        long startTime = System.nanoTime();
        doMergeSort(numCount, 0, fileA, fileB, fileC, fileG);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000000;
        System.out.println("Время выполнения сортировки без оптимизации " + duration + " сек.");

        startTime = System.nanoTime();
        doMergeSort(numCount, inMemorySortSteps, fileA, fileB, fileC, fileG);
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000000;
        System.out.println("Время выполнения сортировки с оптимизацией " + duration + " сек.");
    }

    private static void doMergeSort(int numCount, int inMemorySortSteps, String fileA, String fileB, String fileC, String fileG) throws IOException {
        inMemorySort(inMemorySortSteps, fileG, fileA);
        int iterCnt = (int) Math.ceil(log2(numCount));

        for (int i = inMemorySortSteps; i < iterCnt; i++) {
            int cntInGroup = (int) Math.pow(2, i);
            split(fileA, fileB, fileC, cntInGroup);
            merge(fileA, fileB, fileC, cntInGroup);
        }
    }

    private static void inMemorySort(int groupCnt, String fileG, String fileA) throws IOException {
        FileReader gFile = new FileReader(fileG);
        FileWriter aFile = new FileWriter(fileA, false);

        while (true) {
            int cntOfNums = (int) Math.pow(2, groupCnt);
            ArrayList<Integer> numbers = new ArrayList<>();
            for (int i = 0; i < cntOfNums; i++) {
                Integer num = getNextNum(gFile);
                if (num == null) {
                    break;
                }
                numbers.add(num);
            }
            if (numbers.isEmpty()) {
                break;
            }
            Collections.sort(numbers);
            for (Integer number : numbers) {
                aFile.write(String.valueOf(number));
                aFile.write(" ");
            }
        }

        gFile.close();
        aFile.close();
    }

    private static void merge(String fileA, String fileB, String fileC, int cntInGroup) throws IOException {
        System.out.println("Merging " + cntInGroup);
        FileReader bFile = new FileReader(fileB);
        FileReader cFile = new FileReader(fileC);
        FileWriter aFile = new FileWriter(fileA, false);
        boolean endOfFile = processGroup(cntInGroup, bFile, cFile, aFile);
        while (!endOfFile) {
            endOfFile = processGroup(cntInGroup, bFile, cFile, aFile);
        }

        bFile.close();
        cFile.close();
        aFile.close();
    }

    //    метод возвращает true если дошли до конца файлов (b and c)
    private static boolean processGroup(int cntInGroup, FileReader bFile, FileReader cFile, FileWriter aFile) throws IOException {
        int cntB = 0;
        int cntC = 0;
        Integer bufB = getNextNum(bFile);
        Integer bufC = getNextNum(cFile);
        if (bufB == null) {
            return true;
        }
        while (cntB != cntInGroup && cntC != cntInGroup) {
            if (bufB <= bufC) {
                aFile.write(String.valueOf(bufB));
                aFile.write(" ");
                cntB++;
                if (cntB < cntInGroup) {
                    bufB = getNextNum(bFile);
                }
            } else {
                aFile.write(String.valueOf(bufC));
                aFile.write(" ");
                cntC++;
                if (cntC < cntInGroup) {
                    bufC = getNextNum(cFile);
                }
            }
        }

        if (cntB == cntInGroup) {
            aFile.write(String.valueOf(bufC));
            aFile.write(" ");
            cntC++;
        } else {
            aFile.write(String.valueOf(bufB));
            aFile.write(" ");
            cntB++;
        }

        while (cntB < cntInGroup) {
            bufB = getNextNum(bFile);
            cntB++;
            aFile.write(String.valueOf(bufB));
            aFile.write(" ");
        }
        while (cntC < cntInGroup) {
            bufC = getNextNum(cFile);
            cntC++;
            aFile.write(String.valueOf(bufC));
            aFile.write(" ");
        }
        return false;
    }

    private static void split(String fileA, String fileB, String fileC, int cntInGroup) throws IOException {
        System.out.println("Splitting " + cntInGroup);
        FileReader reader = new FileReader(fileA);
        FileWriter bFile = new FileWriter(fileB, false);
        FileWriter cFile = new FileWriter(fileC, false);

        int cnt = 0;
        while (true) {
            Integer num = getNextNum(reader);
            if (num == null) {
                break;
            }
            cnt++;
            if (Math.ceil((double) cnt / cntInGroup) % 2 == 0) {
                cFile.write(String.valueOf(num));
                cFile.write(" ");
            } else {
                bFile.write(String.valueOf(num));
                bFile.write(" ");
            }
        }

        reader.close();
        bFile.close();
        cFile.close();

    }

    private static Integer getNextNum(FileReader reader) throws IOException {
        int c;
        String buf = "";
        Integer num = null;
        while ((c = reader.read()) != ' ' && (c != -1)) {
            char ch = (char) c;
            buf += ch;
        }
        if (buf != "") {
            num = Integer.parseInt(buf);
        }
        return num;
    }

    private static void generateNumber(String fileA, int numCount) throws IOException {
        FileWriter aFile = new FileWriter(fileA, false);
        for (int i = 0; i < numCount; i++) {
            int num = (int) (100 * Math.random());
            aFile.write(String.valueOf(num));
            aFile.write(" ");
        }
        aFile.close();
    }

    public static double log2(int x) {
        return Math.log(x) / Math.log(2);
    }

}
