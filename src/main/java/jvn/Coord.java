package jvn;

public class Coord {

    public static void main(String[] args) {
        try {
            JvnCoordImpl coord = JvnCoordImpl.getJvnCoordImpl();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
