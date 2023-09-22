import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

public class Agente {

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

        @Override
        public boolean equals(Object object) {
            if(!(object instanceof Posicao other)) return false;
            return other.x == x && other.y == y && other.direcao == direcao;
        }

        public boolean mesmoLugar(Posicao other) {
            return other.x == x && other.y == y;
        }
    }

    Posicao posicaoAtual = new Posicao(0, 0, 0);
    boolean[][] visitados = new boolean[4][4];
    boolean[][] wumpus = new boolean[4][4];
    boolean[][] pocos = new boolean[4][4];
    Deque<Posicao> percurso = new ArrayDeque<>();
    boolean estaComOOuro = false;
    boolean flechasDisponivel = true;

    Queue<Main.Acao> proximaAcao = new LinkedList<>();

    public Agente() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                visitados[i][j] = false;
                wumpus[i][j] = pocos[i][j] = true;
            }
        }
        visitados[0][0] = true;
    }

    public void moverParaFrente() {
        percurso.push(posicaoAtual.copie());
        if (!posicaoAtual.mover()) {
            posicaoAtual = percurso.pop();
            return;
        }
        visitados[posicaoAtual.x][posicaoAtual.y] = true;
    }

    public boolean proximoFoiVisitado() {
        var proximo = posicaoAtual.copie();
        proximo.mover();
        return !proximo.ehValida() || visitados[proximo.x][proximo.y];
    }

    public int rotacoesRealizadas = 0;

    public void sentir(boolean fedor, boolean brisa, boolean brilho, boolean impacto, boolean grito) {
        if(!proximaAcao.isEmpty()) return;
        if(brilho && !estaComOOuro) {
            proximaAcao.add(Main.Acao.AGARRAR);
            estaComOOuro = true;
            return;
        }

        if(grito){
            for (int i = 0; i < 4; i++)
                for (int j = 0; j < 4; j++)
                    wumpus[i][j] = false;
            flechasDisponivel = false;
        }

        var lateral = posicaoAtual.copie();
        for (int i = 0; i < 4; i++) {
            var anterior = lateral.copie();
            lateral.mover();
            if (lateral.ehValida()) {
                wumpus[lateral.y][lateral.x] = fedor && wumpus[lateral.y][lateral.x];
                pocos[lateral.y][lateral.x] = brisa && pocos[lateral.y][lateral.x];
            }
            lateral = anterior;
            lateral.virarParaDireita();
        }
        if (fedor){
            for (int i = 0; i < 4; i++)
                for (int j = 0; j < 4; j++)
                    if ((Math.abs(i - posicaoAtual.y) + Math.abs(j - posicaoAtual.x)) >= 2) wumpus[i][j] = false;
            visitados[posicaoAtual.x][posicaoAtual.y] = false;
        }

        if (estaComOOuro) {
            if(posicaoAtual.estaNoInicio()) {
                proximaAcao.add(Main.Acao.ESCALAR);
                return;
            }

            var posicaoDesejada = percurso.pop();
            moverPara(posicaoDesejada);
            return;
        }

        if (flechasDisponivel && wumpusDeveEstarAli()) {
            proximaAcao.add(Main.Acao.ATIRAR);
        }


        var backup = posicaoAtual.copie();
        int i = 0;
        for(; i < 4; i++){
            if (flechasDisponivel && wumpusDeveEstarAli()) {
                posicaoAtual = backup;
                if(i <= 2){
                    for(int j = 0; j < i; j++){
                        proximaAcao.add(Main.Acao.ROTACAO_DIREITA);
                        posicaoAtual.virarParaDireita();
                    }
                } else {
                    for(int j = 0; j < 1; j++){
                        proximaAcao.add(Main.Acao.ROTACAO_ESQUERDA);
                        posicaoAtual.virarParaEsquerda();
                    }
                }
                proximaAcao.add(Main.Acao.ATIRAR);
                return;
            }
            if (!proximoFoiVisitado() && !proximoMovimentoEhMortal()) break;
            posicaoAtual.virarParaDireita();
        }

        posicaoAtual = backup;

        if (i == 4) {
            if (percurso.isEmpty()) {
                proximaAcao.add(Main.Acao.ESCALAR);
                return;
            }

            var posicaoDesejada = percurso.pop();
            moverPara(posicaoDesejada);
            return;
        }

        if(i <= 2){
            for(int j = 0; j < i; j++){
                proximaAcao.add(Main.Acao.ROTACAO_DIREITA);
                posicaoAtual.virarParaDireita();
            }
        } else {
            for(int j = 0; j < 1; j++){
                proximaAcao.add(Main.Acao.ROTACAO_ESQUERDA);
                posicaoAtual.virarParaEsquerda();
            }
        }
        proximaAcao.add(Main.Acao.MOVER);
        moverParaFrente();

    }

    private void moverPara(Posicao posicaoDesejada) {
        var posicaoDeCalculo = posicaoAtual.copie();
        var posicaoAuxiliar = posicaoAtual.copie();
        posicaoAuxiliar.mover();
        int contarRotacoes = 0;
        while(!posicaoAuxiliar.mesmoLugar(posicaoDesejada)){
            contarRotacoes++;
            posicaoDeCalculo.virarParaDireita();
            posicaoAuxiliar = posicaoDeCalculo.copie();
            posicaoAuxiliar.mover();
        }

        if(contarRotacoes <= 2){
            for(int j = 0; j < contarRotacoes; j++){
                proximaAcao.add(Main.Acao.ROTACAO_DIREITA);
                posicaoAtual.virarParaDireita();
            }
        } else {
            for(int j = 0; j < (4 - contarRotacoes); j++){
                proximaAcao.add(Main.Acao.ROTACAO_ESQUERDA);
                posicaoAtual.virarParaEsquerda();
            }
        }
        posicaoAtual.mover();
        proximaAcao.add(Main.Acao.MOVER);
    }

    public boolean proximoMovimentoEhMortal() {
        var proximo = posicaoAtual.copie();
        proximo.mover();
        return wumpus[proximo.y][proximo.x] || pocos[proximo.y][proximo.x];
    }

    public boolean wumpusDeveEstarAli() {
        var possivelWumpus = posicaoAtual.copie();
        possivelWumpus.mover();
        if (!possivelWumpus.ehValida() || !wumpus[possivelWumpus.y][possivelWumpus.x]) return false;
        int wumpusPossiveis = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (wumpus[i][j]) wumpusPossiveis++;
            }
        }
        return wumpusPossiveis == 1;
    }

    public Main.Acao executarAcao() {
       return proximaAcao.poll();
    }

}
