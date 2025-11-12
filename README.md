creazione di file server e client di allarmi per:
    temperatura magggiore di 35;
    rilevamento di movimento dopo una certa ora;

i client devono inviare le informazione tramite formato json al server;
il server deve salvare le informazioni in un file di log con timestamp.

esempio di formato json:
    
    temperatura{
        id: "sensor_1",
        tipo: "temperatura",
        valore: (double)
    }

    movimento{
        id: "sensor_2",
        tipo: "movimento",
        valore: true/false
        zona: giardino | cucina | bagno
        ora: 
    }

    contatto porta{
        id: "sensor_3",
        tipo: "contatto_porta",
        valore: (boolean)
    }

bozza non so nemmeno se e' corretto sto formato di json, da correggere se ci sono eventuali cambiamenti.v