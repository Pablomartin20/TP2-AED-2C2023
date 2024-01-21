package aed;

public class ColaPrioritaria {
    private Nodo[] _heap;
    private int _capacidad;
    private int _longitud;

    /* Invariante de representación:
     * 
     * 1) _capacidad es la longitud del array que representa al heap.
     * 2) _longitud es un entero menor o igual a _capacidad.
     * 3) El array _heap tiene una cantidad de elementos definidos igual a _longitud. Los
     *    elementos definidos son aquellos con id y votos distintos de -1.
     * 4) Todos los elementos definidos del array conforman un max-heap.
     */

    private class Nodo {
        int vot;
        int id;

        Nodo(int votos, int idPartido) { vot = votos; id = idPartido;}
        Nodo() { vot = id = -1; }
    }

    public ColaPrioritaria(int capacidad) {
        _heap = new Nodo[capacidad];
        _capacidad = capacidad;
        for(int i = 0; i < _capacidad; i++){
            _heap[i] = new Nodo();
        }
        _longitud = 0;
    }

    public ColaPrioritaria(int[] arr, int totales){
        int i = 0;
        while (i<arr.length-1){
            if (((float)arr[i]/totales)* 100 > 3){
                _longitud++;
            }
            i++;
        }
        _capacidad = _longitud;
        Nodo[] heapificado = new Nodo[_capacidad];
        i = 0;
        int k = 0;
        while(i < arr.length-1){
            Nodo nuevo = new Nodo(arr[i],i);
            if (((float)arr[i]/totales)*100.0 <= 3){
                i++;
                continue;
            }
            heapificado[k] = nuevo;
            k++;
            i++;
        }
        for(int j = _capacidad/2 - 1; j >= 0; j--){
            maxHeapificar(heapificado,_capacidad,j);
        }
        _heap = heapificado;
    }

    public void swap(int a, int b) {
        int votAux = _heap[a].vot;
        int idAux = _heap[a].id;
        _heap[a].vot = _heap[b].vot;
        _heap[a].id = _heap[b].id;
        _heap[b].vot = votAux;
        _heap[b].id = idAux;
    }

    private void heapifyUp(int pos) {
        int posActual = pos;
        int padre = (posActual-1)/2;
        while(posActual != 0 && _heap[posActual].vot > _heap[padre].vot){
            swap(posActual,padre);
            int aux = padre;
            posActual = aux;
            padre = (posActual-1)/2;
        }
    }

    private void maxHeapificar(Nodo[] arr, int cap, int i){
        int maximo = i;
        int hijoIzq = 2*i+1;
        int hijoDer = 2*i+2;
        if(hijoIzq < cap && arr[hijoIzq].vot > arr[maximo].vot){
            maximo = hijoIzq;
        }
        if(hijoDer < cap && arr[hijoDer].vot > arr[maximo].vot){
            maximo = hijoDer;
        }
        if(maximo != i){
            Nodo aux = arr[i];
            arr[i] = arr[maximo];
            arr[maximo] = aux;
            maxHeapificar(arr, cap, maximo);
        }
        
    }

    public void encolar(int votos, int id) {
        _longitud++;
        if(_longitud == 1){
            _heap[0].vot = votos;
            _heap[0].id = id;
        } else {
            _heap[_longitud-1].vot = votos;
            _heap[_longitud-1].id = id;
            heapifyUp(_longitud-1);
        }
    }

    public void desencolar() {
        int ultimaHoja = _longitud-1;
        swap(0,ultimaHoja);
        _heap[ultimaHoja].vot = -1;
        _heap[ultimaHoja].id = -1;
        maxHeapificar(_heap,_capacidad,0);
        _longitud--;
    }

    public int proximo() {
        return _heap[0].vot;
    }

    public int proximoPartido() {
        return _heap[0].id;
    }

}





























