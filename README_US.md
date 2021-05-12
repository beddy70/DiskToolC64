# DiskToolC64

This project allows the exchange of files between your local disk and a D64 image (Commodore 64 image disk)

## Introduction

A d64 file is an image of Commodore 64 floppy disk. it's a usefull format to store it on mordern drives. many Commodore 64 hardware and software emulators support these image files

DiskToolC64 tool will help you to :

* easily exchange files between you local drive and an D64 image
* create and format an D64 image
* read/write or delete fil from an D64 image
* show the directory from an D64 image 

DiskToolC64 is written in Java an you'll should install on your system [java runtime 8](https://www.oracle.com/fr/java/technologies/javase-jre8-downloads.html) or [higher](https://www.oracle.com/java/technologies/javase-downloads.html) to use it.

## Utilisation

DiskToolC64 is a command line. lif you launch it without argument you'll get usage information :

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
## Examples

### Create an D64 image

 ```
java -jar DiskToolC64.jar mondisk.d64 -c mydisk

Create drive : mondisk.d64
Format drive : mondisk.d64 named : mydisk
 
 ``` 
 ### Format an D64 image

 ```
java -jar DiskToolC64.jar mondisk.d64 -f empty

Format drive : mondisk.d64 named : empty
 
 ``` 
 ### Write a local file to D64 image 

In [new name] argument don't add extention !

 ```
java -jar DiskToolC64.jar mondisk.d64 -p /users/eddy/c64/games/pacman.prg pacman
 
 ``` 
 
 ### Copier un fichier d'une image D64 sur le disque local

In [cbmfile] argument don't add extention !

 ```
java -jar DiskToolC64.jar mondisk.d64 -g SPRITE .
 
file found : SPRITE.PRG
SPRITE
size=15748 | real size=15653
 ``` 
### Directory of an D64 image
 
 ```
java -jar DiskToolC64.jar mondisk.d64 -d

Drive:'PEA_1541_DRIVE' - Version:0.1

------------------------------------
TEST.PRG                15748 bytes
SPRITE.PRG              15748 bytes
------------------------------------
2 file(s) freespace 137160 bytes
 ```
 
### delete fiel from d64 image
 
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
