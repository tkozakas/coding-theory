import java.util.Arrays;

public record CosetLeader(int[] syndrome, int[] cosetLeader, int weight) {
    @Override
    public String toString() {
        String syndromeStr = Arrays.toString(syndrome);
        String cosetLeaderStr = Arrays.toString(cosetLeader);
        return String.format("%s | %s | %s%n",
                syndromeStr, cosetLeaderStr, weight);
    }
}
