Projekt zur Berechnung des Schwerpunktes und des Statischen Moments der IAM Amöben.

# Tools benutz
- MacBook -> zur entfernung der Bild hintergründe
- StackOverflow -> Code hilfen
- Java -> Programiersprache meiner Wahl
- Ai -> zur Hilfe mit teilen des Codes, darunter die ganze CSV und teile der Visuellen Asugabe, da diese Felder sind die ich vorher nicht benutz habe

  # Hinweise

  Der berechnete Schwerpunkt und das Statische Momentum ist nur eine annäherung, da hier mehrere Probelem auftreten.
  
  Einerseits muss ich drauf hoffen das die Amöben richtug ausgeschnitten worden sind, was bei der einen schonmal etwas schiefgelaufen ist.
  Dazu kommt das Gewicht vom sticker wurde mit 1.01 bewertet, die Fäche wurde mit 1 bewertet in der Rechnung, dies soll die genauikgeit leicht erhöhen.
  Da die Bilder wahrscheinlich keinem Perfekten 90° Winkel entsprechen verzeht dieses nochmals die Berechnungen etwas.

  Bei dem Statischen Moment wird vom Mittelpunkt des Scwhwerpunktes ausgegangen, und diese werden in pixel^2 angegeben.


  # Wie wurde das Problem angegangen?
  Zur berechnung wird hier ein ganz einfaches System benutz.
  Erstmal werden alle Pixel als entweder Amöbe, Sticker oder als nichts katogoriert. <br> Das Programm gewichtet dabei nur Pixel ab einem Alpha wert von 128, um die Transparenten Pixel auszusortieren.
  Diese wird dann in 2 Achsen simplifiziert, X und Y, jeder Pixel wirdd beiden Achsen zugeordnet, bei den Pixel des Stickers wird statt +1 dieser mit 1.01 multipliziert.
  <br>Nun wird einfach der Wert der pixel in X und Y achse durch die Summe der Pixelgewichte dividiert.
  <br><br>
  Also wir gehen in X und Y achse von 0, und finden dann die Mitte der erkannten Pixel.
  <br><br>
  Ich hoffe diese erklärung ergibt Sinn, sie hat bei mir im Kopf mehr Sinn ergeben


  // Visualisuerung:
 <br><br>
   A B C<br>
   D E F<br>
   G H I<br>
  <br>
  Wird zu:
  X : A B C D E F G H I<br>
  Y: A B C D E F G H I<br>

  Wir gehen davon aus das alle von A bis I gleich sind und nicht der Hintergrund sind.

  Diese werden dann mit der Summer der Gewichte dividiert.

# Ergebnisse

#Amöbe 1

Ausgeschnittene Bild:

![bild1](https://github.com/Chaoshero5567/Amobe/blob/main/src/main/resources/Amoebe_1.png)

Bild mit Outline, bewerteter Masse, Schwerbunkt und Momentum

![resultat1](https://github.com/Chaoshero5567/Amobe/blob/main/output/Amoebe_1_result.png)

Hinweis: Wie zu sehen ist ist die Berechnung nicht perfekt da hier ein  Teil unten links fehlerhaft ausgeschnitten wurde und somit nicht mit berechnet wurde. Dies wurde aber teilweise durch erweiterung des erkennungsbereiches behoben.


Schwerpunkt liegt bei: (1763,228; 1432,014)

Statische Momente:

x: 1.0966998437897742E12 p^2 <br>
y: 5.649790519876921E11 p^2


#Amöbe 2

Ausgeschnittene Bild:

![bild2](https://github.com/Chaoshero5567/Amobe/blob/main/src/main/resources/Amoebe_2.png)

Bild mit Outline, bewerteter Masse, Schwerbunkt und Momentum

![resultat2](https://github.com/Chaoshero5567/Amobe/blob/main/output/Amoebe_2_result.png)

Schwerpunkt liegt bei:(1838,249; 1521,855)

Statische Momente:

x: 1.372754036956749E12 p^2 <br>
y: 1.0177687445093416E12 p^2

Runtime Optimierungen haben das Prokekt von 12 auf unter 3 Sekunden gebracht

<img width="387" height="36" alt="image" src="https://github.com/user-attachments/assets/6a71e0c0-8283-4a8b-acf7-ecb16adb24fd" />



  
