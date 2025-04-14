# Sétáljunk

### Mi ez?
#### Ez az app egy egyetemi tantárgy beadandója. Ha gondolod, [issues](https://github.com/domedav/Setaljunk/issues)-ba akár értékelheted is.

### Mit valósítottam meg a beadandó követelményeiből?
- Térkép (Navigáció)
- Szenzorok (Lépésszámláló és Kamera)
- Adattárolás (Shared Prefs)
- Többnyelvűség (Angol és Magyar nyelv)
- Értesítések (emlékeztetők)
- QR-kód alapú "közös séta"
---

Az appot nem tervezem továbbfejleszteni a kellőnél jobban, viszont ha van rá érdeklődő, szívesen teszem ezt.

---

### Letöltés
- Az appot letudod tölteni a [Release](https://github.com/domedav/Setaljunk/releases/tag/v1.0) oldalról.
- Illetve a [Play Áruház](https://play.google.com/store/apps/details?id=com.domedav.setaljunk)-ból.

### Ha buildelni akarod az appot:
- A ```local.properties``` fileba megkell adnod egy api kulcsot ```API_KEY=SajátGoogleApiKey```
- Ezt az api kulcsot a [Google Cloud](https://console.cloud.google.com/apis/)-ban tudod legenerálni.
- Bekell kapcsolnod a ```Directions API```-t, a ```Geocoding API```-t, a ```Routes API```-t és a ```Navigation SDK```-t az új Google Cloud projectedben.
- Ha ezek megvannak, a project api kulcsát másold be a megfelelő helyre, és már működni is fog!
