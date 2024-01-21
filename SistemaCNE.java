package aed;

public class SistemaCNE {

    /* Invariante de representación:
     *
     * 1) max es la cantidad de votos que tiene el partido con mayor cantidad de votos a
     *    presidente en el array votosPresiXPart.
     * 2) secMax es el segundo partido con mayor cantidad de votos a presidente en el
     *    mismo array.
     * 3) votosTotales es la suma de todos los votos a presidente de cada partido, incluyendo
     *    los votos en blanco, del array votosPresiXPart.
     * 4) Los arreglos votosDipuXDist, bancasYNombreDistXDist, ultimasMesasXDist, 
     *    votosDipuXPartXDist, colasModificadas y distribucionDeBancas 
     *    tienen la misma longitud.
     * 5) No hay dos partidos con el mismo nombre o con la misma cantidad de votos en
     *    el array votosPresiXPart.
     * 6) Los arreglos que pertenecen a votosDipuXDist y el arreglo votosPresiXPart 
     *    tienen todos la misma longitud.
     * 6) No hay dos distritos con el mismo nombre en bancasYNombreDistXDist.
     * 7) El array ultimasMesasXDist está ordenado de forma estrictamente creciente.
     * 8) Ninguna cola tiene dos partidos con el mismo ID o con la misma cantidad de votos en
     *    el array de colas colasDipuXPartXDist.
     * 9) Cada cola en el array de colas tiene la longitud del array votosPresiXPart
     *     menos uno, es decir excluyendo los votos en blanco.
     * 10) Los votos a diputados que están en el array votosDipuXDist se corresponden
     *     con los votos que están en el array de colas.
     * 11) Cada subarreglo del arreglo distribucionDeBancas tiene longitud P-1, donde P
     *     es la longitud del arreglo votosPresiXPart.
     * 12) Si algún elemento en colasModificadas es false entonces el array correspondiente
     *     en distribucionDeBancas tiene todos sus elementos en cero, y los votos de la
     *     correspondiente cola en colasDipuXPartXDist permanece inalterada. En caso de ser
     *     true entonces la cantidad de bancas de cada partido en distribucionDeBancas
     *     se corresponde con la distribución con el sistema de D'Hondt.
     * 
     */

    public class infoPresidencial{
        private int votosPresi;
        private String nombrePartido;
        infoPresidencial(int votosPresi, String nombrePartido){
            this.votosPresi = votosPresi;
            this.nombrePartido = nombrePartido;
        }
        public int votosPresi(){return votosPresi;}
        public String nombrePartido(){return nombrePartido;}
    }

    public class infoDistrito{
        private int cantBancas;
        private String nombreDistrito;
        infoDistrito(int cantBancas, String nombreDistrito){
            this.cantBancas = cantBancas;
            this.nombreDistrito = nombreDistrito;
        }
        public int cantBancas(){return cantBancas;}
        public String nombreDistrito(){return nombreDistrito;}
    }

    private int _max;
    /* Cantidad de votos para presidente que tiene el partido más votado. */
    private int _secMax;
    /* Cantidad de votos para presidente que tiene el segundo partido más votado. */
    private int _votosTotalesPresi;
    /* Cantidad de votos totales para presidente. */
    private infoPresidencial[] _votosPresiXPart;
    /* Arreglo donde los índices son los idPartido, y contiene los votos a presidente y el
     * nombre del partido. */
    private boolean[] _colasModificadas;
    /* Arreglo donde los índices son los idDistrito y guardan true si la cola del mismo
     * índice en colasDipuXPartXDist fue modificada alguna vez para calcular la
     * distribución de bancas o false en caso contrario. */
    private int[][] _distribucionDeBancas;
    /* Arreglo donde los índices son los idDistrito y guarda la distribución de bancas
     * por partido. En caso de que el elemento del mismo índice sea false en
     * colasModificadas entonces todos sus elementos serán cero. */
    private int[][] _votosDipuXDist;
    /* Arreglo donde los índices son los idDistrito, y contiene arrays con los votos a
     * diputados de cada partido en ese distrito. */
    private int[] _votosTotalesDipu;
    /* Arreglo donde los índices son los idDistrito, y contiene la cantidad de votos
     * totales para diputados de cada distrito. */
    private infoDistrito[] _bancasYNombreDistXDist;
    /* Arreglo donde los índices son los idDistrito, y contiene cant de bancas en disputa
     * y nombre del distrito. */
    private int[] _ultimasMesasXDist;
    /* Arreglo ordenado donde los índices son los idDistrito y guarda el número de la ultima
     * mesa del intervalo. */
    private ColaPrioritaria[] _colasDipuXPartXDist;
    /* Arreglo donde los índices son los idDistrito y cada distrito tiene una cola, donde 
     * cada elemento es una tupla que contiene la cantidad de votos de un partido y 
     * su correspondiende id. */

    public class VotosPartido{
        private int presidente;
        private int diputados;
        VotosPartido(int presidente, int diputados){this.presidente = presidente; this.diputados = diputados;}
        public int votosPresidente(){return presidente;}
        public int votosDiputados(){return diputados;}
    }

    /* P = Cant de partidos políticos.
     * D = Cant de distritos.
     * D_d = cant de bancas de diputados en el distrito d. Cada distrito d puede tener una 
     * cant D_d distinta.
     */

    // O(P*D):
    // O(P) + 2 O(P*D) + 4 O(D) + O(1) * P + O(1) * D + O(1) = O(P*D)
    public SistemaCNE(String[] nombresDistritos, int[] diputadosPorDistrito, String[] nombresPartidos, int[] ultimasMesasDistritos) {
        _max = 0;
        _secMax = 0;
        _votosTotalesPresi = 0;
        _votosPresiXPart = new infoPresidencial[nombresPartidos.length]; // O(P)
        _votosDipuXDist = new int[nombresDistritos.length][nombresPartidos.length]; // O(D*P)
        _bancasYNombreDistXDist = new infoDistrito[nombresDistritos.length]; // O(D)
        _colasDipuXPartXDist = new ColaPrioritaria[nombresDistritos.length]; // O(D)
        _colasModificadas = new boolean[nombresDistritos.length]; // O(D)
        _distribucionDeBancas = new int[nombresDistritos.length][nombresPartidos.length-1]; // O(D*P)
        _votosTotalesDipu = new int[nombresDistritos.length]; // O(D)
        int i = 0;
        while(i < _votosPresiXPart.length){ // O(P)
            infoPresidencial info = new infoPresidencial(0, nombresPartidos[i]);
            _votosPresiXPart[i] = info;
            i++;
        }
        i = 0;
        while(i < _bancasYNombreDistXDist.length){ // O(D)
            infoDistrito info = new infoDistrito(diputadosPorDistrito[i], nombresDistritos[i]);
            _bancasYNombreDistXDist[i] = info;
            i++;
        }
        _ultimasMesasXDist = ultimasMesasDistritos;
    }
    /* Se inicializan todas las variables de la estructura:
     * - 3 enteros en tiempo constante.
     * - _votosPresiXPart en tiempo lineal respecto de la cantidad de partidos políticos.
     * - _votosDipuXDist es un arreglo de arreglos de tamaño D*P, donde D es la cantidad
     *   de distritos y P la de partidos políticos. Esto es O(D*P).
     * - _bancasYNombreDistXDist, _colasDipuXPartXDist, _colasModificadas y _votosTotalesDipu
     *   en tiempo lineal respecto de la cantidad de distritos.
     * - _distribucionDeBancas es el mismo caso que _votosDipuXDist, pues es de tamaño D*P.
     * - Luego se inicializan _votosPresiXPart con la información presidencial por defecto y
     *   _bancasYNombreDistXDist con la información de cada distrito. Estas últimas en tiempo
     *   lineal respecto de la cantidad de partidos y la cantidad de distritos, respectivamente.
     */

    public String nombrePartido(int idPartido) {
        return _votosPresiXPart[idPartido].nombrePartido;
    }
    // Se devuelve una variable de la estructura en tiempo constante.

    public String nombreDistrito(int idDistrito) {
        return _bancasYNombreDistXDist[idDistrito].nombreDistrito;
    }
    // Se devuelve una variable de la estructura en tiempo constante.

    public int diputadosEnDisputa(int idDistrito) {
        return _bancasYNombreDistXDist[idDistrito].cantBancas;
    }
    // Se devuelve una variable de la estructura en tiempo constante.

    private int buscarDistrito(int inicio, int fin, int idMesa){
        if(fin - inicio == 1){
            if(idMesa < _ultimasMesasXDist[inicio]){
                return inicio;
            } else {
                return fin;
            }
        } else {
            int medio = (inicio+fin)/2;
            if(idMesa < _ultimasMesasXDist[medio]){
                return buscarDistrito(inicio, medio, idMesa);
            } else if(idMesa > _ultimasMesasXDist[medio]){
                return buscarDistrito(medio, fin, idMesa);
            } else {
                return medio+1;
            }
        }
    }

    public String distritoDeMesa(int idMesa) {
        int distrito = buscarDistrito(0,(_ultimasMesasXDist.length)-1,idMesa);
        return _bancasYNombreDistXDist[distrito].nombreDistrito();
    }
    // Se realiza una búsqueda binaria en un arreglo ordenado de longitud D, de ahí la complejidad.

    public void registrarMesa(int idMesa, VotosPartido[] actaMesa) {
        int idDistrito = buscarDistrito(0,(_ultimasMesasXDist.length-1),idMesa); // O(log(D))
        int i = 0;
        while(i < actaMesa.length){ // O(P)
            _votosPresiXPart[i].votosPresi += actaMesa[i].votosPresidente();
            _votosDipuXDist[idDistrito][i] += actaMesa[i].votosDiputados();
            _votosTotalesPresi += actaMesa[i].votosPresidente();
            _votosTotalesDipu[idDistrito] += actaMesa[i].votosDiputados();
            i++;
        }
        ColaPrioritaria heap = new ColaPrioritaria(_votosDipuXDist[idDistrito], _votosTotalesDipu[idDistrito]); // O(P)
        _colasDipuXPartXDist[idDistrito] = heap;
        if(_votosPresiXPart[0].votosPresi > _votosPresiXPart[1].votosPresi){
            _max = _votosPresiXPart[0].votosPresi;
            _secMax = _votosPresiXPart[1].votosPresi;
        } else {
            _max = _votosPresiXPart[1].votosPresi;
            _secMax = _votosPresiXPart[0].votosPresi;
        }
        i = 0;
        while(i < actaMesa.length-1){ // O(P)
            if(_votosPresiXPart[i].votosPresi > _max){
                _secMax = _max;
                _max = _votosPresiXPart[i].votosPresi;
            } else if(_votosPresiXPart[i].votosPresi > _secMax){
                if(_votosPresiXPart[i].votosPresi != _max){
                    _secMax = _votosPresiXPart[i].votosPresi;
                }
            }
            i++;
        }
        if (_colasModificadas[idDistrito]){
            int[] reset = new int[actaMesa.length -1];
            _distribucionDeBancas[idDistrito] = reset;
            _colasModificadas[idDistrito] = false;
        }
    }
    /* - En primer lugar, se realiza una búsqueda binaria del distrito en tiempo logarítmico
     *   respecto de la cantidad de distritos. 
     * - Una vez se obtuvo el distrito, sumamos los votos totales de cada partido
     *   tanto para las presidenciales como para las legislativas, en tiempo lineal
     *   respecto de la cantidad de partidos.
     * - Luego, creamos la cola de prioridad cuyo constructor filtra los partidos que
     *   no superan el umbral en tiempo lineal respecto de la cantidad de partidos.
     * - Después, actualizamos la cantidad de votos que tiene el partido más votado y el segundo
     *   más votado para las presidenciales, en tiempo lineal respecto de la cantidad de partidos.
     * - Finalmente, dado que se registraron los votos de una nueva mesa, reseteamos
     *   la distribución de bancas del distrito en cuestión en tiempo constante.
     *   O(log(D) + 3*P + 1) = O(P + log(D))
     */

    public int votosPresidenciales(int idPartido) {
        return _votosPresiXPart[idPartido].votosPresi;
    }
    // Se devuelve una variable de la estructura en tiempo constante.

    public int votosDiputados(int idPartido, int idDistrito) {
        return _votosDipuXDist[idDistrito][idPartido];
    }
    // Se devuelve una variable de la estructura en tiempo constante.

    public int[] resultadosDiputados(int idDistrito){
        if(!_colasModificadas[idDistrito]){
            _colasModificadas[idDistrito] = true;
            int D_d = diputadosEnDisputa(idDistrito);
            int bancasAsignadas = 0;
            while(bancasAsignadas < D_d){ // O(D_d*log(P))
                int part = _colasDipuXPartXDist[idDistrito].proximoPartido();
                int bancasActuales = 0;
                int votosOrig = _votosDipuXDist[idDistrito][part];
                _colasDipuXPartXDist[idDistrito].desencolar(); // O(log(P))
                _distribucionDeBancas[idDistrito][part]++;
                bancasActuales = _distribucionDeBancas[idDistrito][part];
                _colasDipuXPartXDist[idDistrito].encolar(votosOrig/(bancasActuales+1),part); // O(log(P))
                bancasAsignadas++;
            }
        }
        return _distribucionDeBancas[idDistrito];
    }
    /* Se filtraron aquellos partidos que no superan el umbral y únicamente están en la cola los
       que sí.
       Como consecuencia, el ciclo realiza D_d iteraciones y cada una contiene operaciones con
       costo temporal constante y operaciones con costo logarítmico respecto de
       la longitud de la cola, y tenemos que cumplimos con la cota temporal pedida. 
       O(D_d*(2*log(P) + 1)) = O(D_d*log(P))
     */

    public boolean hayBallotage(){
        float porcentajePrim = ((float)_max/_votosTotalesPresi)*100;
        float porcentajeSeg = ((float)_secMax/_votosTotalesPresi)*100;
        float dif = (porcentajePrim - porcentajeSeg);
        return !(porcentajePrim >= 45 || (porcentajePrim >= 40 && dif >= 10));
    }
    /* Se realizan cálculos en base a enteros que son variables de la estructura, 
    por lo que se obtiene un tiempo constante. */

}

