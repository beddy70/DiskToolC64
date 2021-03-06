# DiskToolC64

Ce projet permet l'échange de fichiers entre votre disque local et une image D64 (Image de disquette du Commodore 64).

## Introduction

Les fichiers avec l'extension .D64 correspondent à des images de disquette de Commdore 64. ils permettent de stocker facilement des disquettes sur des supports modernes et surtout ils sont utilisés par l'ensemble des émulateurs Commdore 64 software comme hardware. 

l'outils proposé ici, vous permettra :

* d'échanger facilement des fichiers entre votre disque local et l'image d64
* de créer et formater une image D64
* d'écrire/lire ou supprimer des fichiers dans une image D64
* de lister les fichiers contenus dans l'image D64

DiskToolC64 est écrit en java et demande l'installation sur votre système de [java runtime 8](https://www.oracle.com/fr/java/technologies/javase-jre8-downloads.html) ou [suppérieur](https://www.oracle.com/java/technologies/javase-downloads.html)

## Utilisation

DiskToolC64 se lance en ligne de commande. la commande suivante (sans argument) vous retourne son usage :

 ```
java -jar DiskToolC64.jar 
 
 ```  
Usage :

 ```
Usage:
 [D64 image file] -c [drivename]                       -> create and format disk
 [D64 image file] -f [drivename]                       -> format disk
 [D64 image file] -d                                   -> read directory
 [D64 image file] -p [local file] [new name]           -> put file
 [D64 image file] -g [cbm file]   [local path]         -> get file
 [D64 image file] -r [cbm file]                        -> remove file
(c) Written by Eddy Briere (2019) - peassembler@yahoo.fr http://somanybits.com
 ``` 
## Exemples

### Création d'une image D64

 ```
java -jar DiskToolC64.jar mondisk.d64 -c mydisk

Create drive : mondisk.d64
Format drive : mondisk.d64 named : mydisk
 
 ``` 
 ### Formatage d'une image D64

 ```
java -jar DiskToolC64.jar mondisk.d64 -f empty

Format drive : mondisk.d64 named : empty
 
 ``` 
 ### Ecrire un fichier local dans une image D64

Dans l'argument [new name] n'ajoutez pas l'extension!

 ```
java -jar DiskToolC64.jar mondisk.d64 -p /users/eddy/c64/games/pacman.prg pacman
 
 ``` 
 
 ### Copier un fichier d'une image D64 sur le disque local

Dans l'argument [cbmfile] n'ajoutez pas l'extension!

 ```
java -jar DiskToolC64.jar mondisk.d64 -g SPRITE .
 
file found : SPRITE.PRG
SPRITE
size=15748 | real size=15653
 ``` 
### Lister les fichiers contenus dans une image D64
 
 ```
java -jar DiskToolC64.jar mondisk.d64 -d

Drive:'PEA_1541_DRIVE' - Version:0.1

------------------------------------
TEST.PRG                15748 bytes
SPRITE.PRG              15748 bytes
------------------------------------
2 file(s) freespace 137160 bytes
 ```
 
### Supprimer un fichier dans une image D64
 
 ```
java -jar DiskToolC64.jar mondisk.d64 -r PIPO

PIPO removed
 ```
Si vous lister l'image D64, vous pourrez noter que PIPO à une nouvelle extension [.DEL]. Le fichier est toujours présent dans l'image mais il ne consome plus d'espace et sera supprimer par l'ajout d'un nouveau fichier:

 ```
java -jar DiskToolC64.jar mondisk.d64 -d
Drive:'PEA_1541_DRIVE' - Version:0.1

------------------------------------
TEST.PRG                15748 bytes
SPRITE.PRG              15748 bytes
PIPO.DEL                15748 bytes
------------------------------------
3 file(s) freespace 137160 bytes
 ```
