package ie.adampurser.lottohub;


public class DrawType {
    public static final String LOTTO = "Lotto";
    public static final String LOTTO_PLUS_1 = "Lotto Plus 1";
    public static final String LOTTO_PLUS_2 = "Lotto Plus 2";
    public static final String LOTTO_PLUS_RAFFLE = "Lotto Plus Raffle";
    public static final String DAILY_MILLION = "Daily Million";
    public static final String DAILY_MILLION_PLUS = "Daily Million Plus";
    public static final String EURO_MILLIONS = "Euro Millions";
    public static final String EURO_MILLIONS_PLUS = "Euro Millions Plus";
    /* return the list of associated draws with the given primary draw */
    public static String[] getAssociatedDrawTypes(String primaryDrawType) {
        String[] drawTypes = null;
        switch (primaryDrawType) {
            case LOTTO:
                drawTypes = new String[4];
                drawTypes[0] = LOTTO;
                drawTypes[1] = LOTTO_PLUS_1;
                drawTypes[2] = LOTTO_PLUS_2;
                drawTypes[3] = LOTTO_PLUS_RAFFLE;
                break;
            case DAILY_MILLION:
                drawTypes = new String[2];
                drawTypes[0] = DAILY_MILLION;
                drawTypes[1] = DAILY_MILLION_PLUS;
                break;
            case EURO_MILLIONS:
                drawTypes = new String[2];
                drawTypes[0] = EURO_MILLIONS;
                drawTypes[1] = EURO_MILLIONS_PLUS;
                break;
        }
        return drawTypes;
    }
    public static String[] getPrimaryDrawTypes() {
        String[] primaryDrawTypes = new String[3];
        primaryDrawTypes[0] = LOTTO;
        primaryDrawTypes[1] = DAILY_MILLION;
        primaryDrawTypes[2] = EURO_MILLIONS;
        return primaryDrawTypes;
    }
}
