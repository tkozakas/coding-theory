import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class UserInterface {
    private final EncoderDecoder encoderDecoder = new EncoderDecoder();
    private final Random random = new Random();
    private final Scanner scanner = new Scanner(System.in);

    private int[][] G;
    private int[] m;
    private int n;
    private int k;

    private final int PROBABILITY = 10; // % error probability

    public UserInterface() {
        prompt();
    }

    public void prompt() {
        /*
           Generuojanti matrica: k - eilučių skaičius (pradines informacijos ilgis), n - stulpelių skaičius (užkoduoto vektoriaus ilgis)
           Užkoduotas vektorius = Pradinis informacijos vektorius (m) * Generuojanti matrica (G)
           Generuojanti matrica turi būti standartinio pavidalo - tokia matrica, kurios kairėje pusėje yra vienetinė matrica (k x k),
           o likusioje dalyje - bet kokie skaičiai

           Vartotojas turi tureti galimybe pačiam įvesti generuojančią matricą, arba leisti programai ją sugeneruoti su k ir n parametrais
         */
        menu();
        scanner.nextLine();
        System.out.println("Enter the vector to encode:");
        m = scanner.nextLine().chars()
                .filter(Character::isDigit)
                .map(c -> c - '0')
                .toArray();

        sendVector();
    }

    private void menu() {
        System.out.print("""
                Choose an option:
                1. Enter generating matrix
                2. Generate generating matrix
                Choice:\s"""
        );
        int choice = scanner.nextInt();

        switch (choice) {
            case 1 -> enterMatrix();
            case 2 -> generateMatrix();
        }
    }

    private void sendVector() {
        int[] c = encoderDecoder.encode(G, m);
        int[] r = encoderDecoder.error(c, PROBABILITY);
        int[][] H = encoderDecoder.parityCheckMatrix(G);
        List<CosetLeader> cosetLeaders = encoderDecoder.findCosetLeaders(H);
        int[] decoded = encoderDecoder.decode(r, H, cosetLeaders);

        System.out.println("Vector to encode: " + vectorAsString(m));
        System.out.println("Generating matrix:\n" + matrixAsString(G));
        System.out.println("Encoded vector: " + vectorAsString(c));
        System.out.println("Received vector: " + vectorAsString(r));
        System.out.println("Parity check matrix:\n" + matrixAsString(H));
        System.out.println("Coset leaders:\nSyndrome | Coset leader | Weight\n" + cosetLeaders);
        System.out.println("Decoded vector: " + vectorAsString(decoded));
    }

    private void generateMatrix() {
        /*
        G = (g11, g12, ..., g1k, | g1(k+1), ..., g1n)
            (g21, g22, ..., g2k, | g2(k+1), ..., g2n)
            (..., ..., ..., ..., | ..., ..., ..., ...)
            (gk1, gk2, ..., gkk, | gk(k+1), ..., gkn)
            <------- k ------->  | <----- n - k ---->

        jeigu k = 5, n = 7, tai
        G = (1, 0, 0, 0, 0, | 1, 0)
            (0, 1, 0, 0, 0, | 0, 1)
            (0, 0, 1, 0, 0, | 1, 1)
            (0, 0, 0, 1, 0, | 1, 0)
            (0, 0, 0, 0, 1, | 0, 1)
                              <-R-> Random
        */
        scanner.nextLine();
        System.out.print("Enter the number of columns (n): ");
        n = scanner.nextInt();
        System.out.print("Enter the number of rows (k): ");
        k = scanner.nextInt();

        G = new int[k][n];

        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                G[i][j] = i == j ? 1 : 0;
            }
            for (int j = k; j < n; j++) {
                G[i][j] = random.nextInt(2);
            }
        }
    }

    private void enterMatrix() {
        System.out.print("Enter the number of columns (n): ");
        n = scanner.nextInt();
        System.out.print("Enter the number of rows (k): ");
        k = scanner.nextInt();

        G = new int[k][n];

        if (k > n) {
            System.out.println("Error: The number of rows (k) should be less than or equal to the number of columns (n).");
            enterMatrix();
        }
        System.out.println("Enter the matrix:");
        scanner.nextLine();
        for (int i = 0; i < k; i++) {
            G[i] = scanner.nextLine().chars()
                    .filter(Character::isDigit)
                    .map(c -> c - '0')
                    .toArray();
        }
    }

    private String matrixAsString(int[][] matrix) {
        return "[ " + Arrays.stream(matrix)
                .map(row -> Arrays.stream(row)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(", ")))
                .collect(Collectors.joining("\n")) + " ]";
    }


    private String vectorAsString(int[] vector) {
        return "[" + Arrays.stream(vector)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(", ")) + "]";
    }
}
