public class Main {

    private static final String[][] mapa = new String[][] {
            {"",   "", "p", ""},
            {"",   "", "", ""},
            {"w", "p", "", ""},
            {"o",  "", "", "p"}
    };

    private static void inicializarMapa(){
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if(mapa[i][j].matches(".*w.*")){
                    if(i > 0) mapa[i - 1][j] += "f";
                    if(i < 3) mapa[i + 1][j] += "f";
                    if(j > 0) mapa[i][j - 1] += "f";
                    if(j < 3) mapa[i][j + 1] += "f";
                }
                if(mapa[i][j].matches(".*p.*")){
                    if(i > 0) mapa[i - 1][j] += "b";
                    if(i < 3) mapa[i + 1][j] += "b";
                    if(j > 0) mapa[i][j - 1] += "b";
                    if(j < 3) mapa[i][j + 1] += "b";
                }
            }
        }
    }

    public enum Acao {
        ROTACAO_DIREITA,
        ROTACAO_ESQUERDA,
        MOVER,
        AGARRAR,
        ATIRAR,
        ESCALAR
    }

    public static class Posicao {
        int x, y, direcao;
        int[][] direcoes = new int[][]{{0, 1}, {-1, 0}, {0, -1}, {1, 0}};

        public boolean estaNoInicio() {
            return x == 0 && y == 0;
        }

        public void virarParaDireita() {
            direcao = (direcao + 3) % direcoes.length;
        }

        public void virarParaEsquerda() {
            direcao = (++direcao) % direcoes.length;
        }

        Posicao(int x, int y, int direcao) {
            this.x = x;
            this.y = y;
            this.direcao = direcao;
        }

        Posicao copie() {
            return new Posicao(x, y, direcao);
        }

        boolean ehValida() {
            return x >= 0 && x < 4 && y >= 0 && y < 4;
        }

        boolean mover() {
            x += direcoes[direcao][1];
            y += direcoes[direcao][0];
            return ehValida();
        }
    }

    private static Posicao robo = new Posicao(0, 0, 0);
    private static boolean AgenteComOuro = false;

    private static void printMapa() {
        String[] flechas = {">", "^", "<", "v"};
        for (int i = 0; i < 4; i++) {
            System.out.print("[");
            for (int j = 0; j < 4; j++) {
                String seta = "";
                if(robo.x == j && robo.y == i) {
                    seta = flechas[robo.direcao];
                    if(AgenteComOuro) seta += "o";
                }
                System.out.printf("\"%3s\"", mapa[i][j].replaceAll("[fb]", "") + seta);
            }
            System.out.print("]\n");
        }
        System.out.print("\n");
    }

    public static void main(String[] args) throws InterruptedException {
        inicializarMapa();
        Agente agente = new Agente();
        agente.sentir(false, false, false, false, false);
        int pontuacaoAgente = 0;

        System.out.println();
        printMapa();

        mainLoop:
        while(pontuacaoAgente > -10000) {
            Thread.sleep(1000);
            Acao acao = agente.executarAcao();
            pontuacaoAgente--;
            System.out.printf("Acao: %s Pontução %d\n", acao.toString(), pontuacaoAgente);
            int x = robo.x;
            int y = robo.y;
            if(mapa[y][x].matches(".*[wp].*")) {
                pontuacaoAgente -= 10000;
                System.out.println("Agente morto");
                break;
            }
            boolean fedor = false;
            boolean brisa = false;
            boolean brilho = false;
            boolean grito = false;
            boolean impacto = false;
            switch (acao) {
                case ESCALAR -> {
                    if (robo.x + robo.y == 0) {
                        break mainLoop;
                    }
                }
                case MOVER -> {
                    var anterior = robo.copie();
                    if (!robo.mover()) {
                        robo = anterior;
                        impacto = true;
                    }
                }
                case AGARRAR -> {
                    AgenteComOuro = mapa[y][x].matches(".*o.*");
                    if(AgenteComOuro) mapa[y][x] = mapa[y][x].replaceAll("o", "");
                }
                case ATIRAR -> {
                    grito = disparar(robo.copie());
                }
                case ROTACAO_DIREITA -> robo.virarParaDireita();
                case ROTACAO_ESQUERDA -> robo.virarParaEsquerda();
            }
            fedor = mapa[robo.y][robo.x].matches(".*f.*");
            brisa = mapa[robo.y][robo.x].matches(".*b.*");
            brilho = mapa[robo.y][robo.x].matches(".*o.*");
            agente.sentir(fedor, brisa, brilho, impacto, grito);
            printMapa();
        }
        System.out.println("Agente terminou");
        if(agente.estaComOOuro && agente.posicaoAtual.estaNoInicio()) pontuacaoAgente += 100;
        System.out.printf("Pontução total: %d\n", pontuacaoAgente);
    }

    public static boolean disparar(Posicao tiro) {
        tiro.mover();
        if(mapa[tiro.y][tiro.x].matches(".*w.*")){
            System.out.println("Wumpus morto");
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    mapa[i][j] = mapa[i][j].replaceAll("[fw]", "");
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
