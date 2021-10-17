package jvn;

public class Coord {

    public static void main(String[] argv) {
        for (String arg : argv) {
            if(arg.equals("-v")){
                JvnGlobals.debug = true;
            }
        }
        
        try {
            JvnCoordImpl coord = JvnCoordImpl.getJvnCoordImpl();

            while(true);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
