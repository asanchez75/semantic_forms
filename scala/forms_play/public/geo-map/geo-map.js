/* TODO pass to Scala.js ,
 * see Scala.js bindings for Leaflet.js : https://github.com/fancellu/scalajs-leaflet
 * API http://leafletjs.com/reference-1.2.0.html
*/

/*global L */

/*private*/
function computeMinInPoints(initialData, key/*: String*/) {
  window.console.log( "computeMinInPoints: initialData: " + initialData )

          var min = Infinity
          for(let dataKey in initialData){
            let element = initialData[dataKey]
//      	        console.log( 'computeMinInPoints 1 key: ' + key + " - element " + JSON.stringify(element) )
//      	        console.log( 'computeMinInPoints 1.1 element[key] ' + (element[key]) )
                  if (Number(element[key]) < min) {
                      min = Number(element[key])
//            	        console.log( 'computeMinInPoints 2' + element )
                  }
            }
            return min
        }
        /*private*/
        function computeMaxInPoints(initialData, key/*: String*/) {
          var max = - Infinity
          for(let dataKey in initialData){
            let element = initialData[dataKey]
//      	        console.log( 'computeMaxInPoints 3.1 element[key] ' + (element[key]) )
              if (Number(element[key]) > max) {
                max = Number(element[key])
              }
            }
            return max
        }

        /** @return Bounds object from LeafLet */
        function findGeographicZone(initialData,keyLat,keyLong) {

//  	        console.log( 'findGeographicZone: initialData: ' +
//  	        		JSON.stringify(initialData) )
//  	        console.log( 'findGeographicZone: computeMinInPoints keyLat: ' +
//  	        		computeMinInPoints(initialData, keyLat) )
//  	        console.log( 'findGeographicZone: computeMaxInPoints keyLat: ' +
//  	        		computeMaxInPoints(initialData, keyLat) )

            var result = L.latLngBounds(
                      L.latLng(
                        computeMinInPoints(initialData, keyLat),
                          computeMaxInPoints(initialData, keyLong)	),
                      L.latLng(
                            computeMaxInPoints(initialData, keyLat),
                            computeMinInPoints(initialData, keyLong)	) )

//  	        console.log( 'findGeographicZone: ' +
//  	        		computeMaxInPoints(initialData, keyLong) + " , " +
//  	        		computeMinInPoints(initialData, keyLong) + " , " +
//  	        		JSON.stringify( result )
//  	        )

            return result
        }

/*global Map*/
class Map{

    /**
     *
     * @param mapId -- id de la div
     * ces quatre paramètres permettre, a l'affichage d'une page, d'initialiser un certain contenu
     * @param initialData: Array -- données que tu veux afficher
     * @param keyLat: String -- clé pour accéder à la valeur :latitude
     * @param keyLong: String -- clé pour accéder à la valeur :longitude
     * @param keyText: String -- clé pour accéder à la valeur :text affiché
     */
    constructor(mapId,initialData,keyLat,keyLong,keyText) {

      var bounds = findGeographicZone(initialData,keyLat,keyLong)
      console.log("bounds")
      console.log(bounds)
      this.OSM = L.map(mapId) .fitBounds( bounds )
        // .setView([48.862725, 2.287592000000018], 10);

        this.pins = []
        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(this.OSM )
        this.keyLat = keyLat
        this.keyLong = keyLong
        this.keyLat = keyLat
        for(let key in initialData){
            if (initialData.hasOwnProperty(key)) {
                this.addPin(initialData[key][keyLat],initialData[key][keyLong],key,initialData[key][keyText])
            }
        }
    }
    /**
     *
     * @param mapId -- id de la div
     * transforme l'array en un objet avant de créer une Map
     * @param initialData -- données que tu veux afficher (type array)
     * @param keyId -- clé pour accéder à la valeur :id
     * @param keyLat -- clé pour accéder à la valeur :latitude
     * @param keyLong -- clé pour accéder à la valeur :longitude
     * @param keyText -- clé pour accéder à la valeur :text affiché
     */
    static constructorWithArray(mapId, initialData,keyId,keyLat,keyLong,keyText) {
//        console.log('in constructor: initialData'); console.log(initialData);
      let objectFromArray = {}
        initialData.forEach(
            function (element) {
            objectFromArray[element[keyId]] = element
        })
        console.log( "constructorWithArray: objectFromArray: " ) ; console.log( objectFromArray )
        return new Map(mapId, objectFromArray, keyLat,keyLong,keyText)
    }

    /**
     * affiche tous les points enregistré
     * @param latitude -- la latitude du point
     * @param longitude -- la latitude du point
     * @param key -- la clé pour la sauvegarde du point
     * @param text -- le texte
     */
    pinShowAll() {
        "use strict"
        for (let key in this.pins) {
            if (this.pins.hasOwnProperty(key)) {
                this.pinShow(key)

            }
        }
    }

    pinShow(key){
        "use strict"
        var pin = this.pins[key]
        if( pin != undefined)
          pin.addTo(this.OSM)
        else
          console.log("Error: pinShow: pin undefined for key " + key)
    }


    addPin(latitude,longitude, key, text) {
      "use strict"
      var pinText = text
      console.log('addPin key '); console.log( key )
      if( key.length > 0 )
        pinText = '<a href="/display?displayuri=' + key + '" target="_blank">' + text + '</a>'
      this.pins[key] = L.marker([latitude,longitude])
          .bindPopup(pinText)
      this.pinShow(key)
    }

    /**
     * affiche un point et efface les autres
     * @param latitude -- la latitude du point
     * @param longitude -- la latitude du point
     * @param key -- la clé pour la sauvegarde du point
     * @param text -- le texte
     */
    pinShowOne(key) {
        "use strict"
        this.pinHideAll()
        this.pinShow(key)
    }

    /**
     * efface un point
     * @param key (uri) -- la clé du point a ne plus afficher
     */
    pinHide(key) {
        "use strict"
        if(this.pins[key] !== undefined){
            let marker = this.pins[key]
            this.OSM.removeLayer(marker)
        }
    }

    /**
     * efface tous les points
     */
    pinHideAll() {
        "use strict"
        for (let key in this.pins){
            if (this.pins.hasOwnProperty(key)) {
                this.pinHide(key)
            }
        }
    }

}
