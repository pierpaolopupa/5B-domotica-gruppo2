Creazione di file server e client di allarmi per:
    temperatura magggiore di 35;
    rilevamento di movimento dopo una certa ora;

I client devono identificarsi e inviare le informazioni tramite formato json al server;
il server deve salvare le informazioni in un file di log con timestamp.

esempio di formato json:
{
    {
        id: "1",
        nome: "sensor_1"
        tipo: "temperatura",
        valore: (double)
    }
}
