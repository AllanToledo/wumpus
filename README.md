# wumpus
Trabalho de Inteligência Artificial com o objetivo de criar um
agente lógico para explorar a caverna do mundo do wumpus.

## Estrutura do projeto
Esse projeto está divido em apenas duas classes, Main e Agente
o problema foi modelado de maneira similar proposta pelo livro
que recomendo uma ideia de Cliente / Servidor, onde o cliente
é o robo que está explorando a caverna e consulta o servidor para
tomar ações, passando como informações os sentidos captados
pelo robo.

### Agente
A classe agente possui assume inicialmente que todos os lugares
possuem poços e wumpus. A medida que o robô explora e passa os
sentidos ao agente, o agente entende quais lugares são seguros
e os considera nas próximas explorações do robo. O agente apenas
dispara se ele houver absoluta certeza que onde o Wumpus está.

### Main
A classe principal simula o ambiente da caverna, validando os 
movimentos do agente e repassando os sentidos obtidos pelo robo.
