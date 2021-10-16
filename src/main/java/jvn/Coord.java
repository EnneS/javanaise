package jvn;

public class Coord {

    public static void main(String[] args) {
        try {
            JvnCoordImpl coord = JvnCoordImpl.getJvnCoordImpl();

            while(true);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
