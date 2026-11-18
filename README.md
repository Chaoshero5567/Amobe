Projekt zur Berechnung des Schwerpunktes und des Statischen Moments der IAM Amöben.

# Tools benutz
- MacBook -> zur entfernung der Bild hintergründe
- StackOverflow -> Code hilfen
- Java -> Programiersprache meiner Wahl

  # Hinweise

  Der berechnete Schwerpunkt und das Statische Momentum ist nur eine annäherung, da hier mehrere Probelem auftreten.
  
  Einerseits muss ich drauf hoffen das die Amöben richtug ausgeschnitten worden sind, was bei der einen schonmal etwas schiefgelaufen ist.
  Dazu kommt das Gewicht vom sticker das nicht mitbeachtet wurde, die Fäche wurde genau wie der Rest mit 1 bewertet in der Rechnung, obwohl der Sticker etwas an Gewicht mehr hat, in einer späteren Revision werde ich diese mit 1.01
  berechnen für eine bessere annäherung.
  Da die Bilder wahrscheinlich keinem Perfekten 90° Winkel entsprechen verzeht dieses nochmals die Berechnungen etwas.

  Bei dem Statischen Moment wird vom Mittelpunkt des Scwhwerpunktes ausgegangen, und diese werden in pixel^2 angegeben.



  - Der CSV code wurde mit Hilfe von KI geschrieben da ich noch nie mit einer CSV gearbeitet habe. Diese werden aber nicht aktiv zur berechnung benutzt und sind mehr ein nettes Extra.


# Ergebnisse

Amöbe 1

Ausgeschnittene Bild:

![bild1](https://github.com/Chaoshero5567/Amobe/blob/main/src/main/resources/Amoebe_1.png)

Bild mit Outline, bewerteter Masse, Schwerbunkt und Momentum

![resultat1](https://github.com/Chaoshero5567/Amobe/blob/main/output/Amoebe_1_result.png)

Hinweis: Wie zu sehen ist ist die Berechnung nicht perfekt da hier ein großer Teil unten links fehlerhaft ausgeschnitten wurde und somit nicht mit berechnet wurde.

Schwerpunkt liegt bei: (1773,62, 1421,68)
Statische Momente:
x: 1.0733194925535835E12 p^2
y: 5.4237897599370233E11 p^2



  
