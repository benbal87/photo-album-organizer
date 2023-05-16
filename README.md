### Photo Album Organizer:

This is a mini project with the goal to rename all image and video files in one directory and all of its subdirectories
and correcting dates of video files.
The directories which containing the media files have to be named customized.

* The names of these directories will be the root names of the media files.
* Then the media file with the latest creation date in a directory will be the reference for the start of the media file
  names with the following format: "YYYY-MM-DD".
* The files then will be numbered with the following format if there are not more than 999 in a directory: 001, 002,
  etc.
* The video files will get a "video" string before their numbers.
* All the files will be numbered by their creation dates.

After the renaming of the files has been finished the HandBrake converting of the video files will start.

* It will convert the video files with the quality of 28.
* All the video files converted with HandBrake will get a "__HB" string added to their file names.
* The original video files would not be deleted.

After the HandBrake process has been finished the exiftool will correct the creation dates of the video files converted
with HandBrake.

### Project Setup on Windows

#### Install HandBrakeCLI

1. Download HandBrakeCLI: https://handbrake.fr/downloads2.php
2. Extract it and put the extracted file to an arbitrary folder.
3. Open the environment variables and create a new entry in the Paths with the containing folder of the HandBrakeCLI.
4. Open a new command prompt window and try to type HandBrakeCLI.
5. More information of the usage of the software can be found
   here: https://handbrake.fr/docs/en/latest/cli/command-line-reference.html

#### Install exiftool

1. Download exiftool windows executable from its website: https://exiftool.org/
2. Extract it and put the extracted file to an arbitrary folder.
3. Rename the file to **exiftool.exe**
4. Open the environment variables and create a new entry in the Paths with the containing folder of the **exiftool.exe**
5. Open a new command prompt window and try to type **exiftool**

Exiftool documentation:

    exiftool -time:all -G1 -a -s file.jpg
    That will list all the time based tags.  Look down the list to figure out which is the correct on and use that name instead of FileCreateDate in your listed command.
    
    -charset [[TYPE=]CHARSET]
    If TYPE is "ExifTool" or not specified, this option sets the ExifTool
    character encoding for output tag values when reading and input
    values when writing. The default ExifTool encoding is "UTF8". If no
    CHARSET is given, a list of available character sets is returned.
    Valid CHARSET values are:
    
     CHARSET     Alias(es)        Description
     ----------  ---------------  ----------------------------------
     UTF8        cp65001, UTF-8   UTF-8 characters (default)
     Latin       cp1252, Latin1   Windows Latin1 (West European)
     Latin2      cp1250           Windows Latin2 (Central European)
     Cyrillic    cp1251, Russian  Windows Cyrillic
     Greek       cp1253           Windows Greek
     Turkish     cp1254           Windows Turkish
     Hebrew      cp1255           Windows Hebrew
     Arabic      cp1256           Windows Arabic
     Baltic      cp1257           Windows Baltic
     Vietnam     cp1258           Windows Vietnamese
     Thai        cp874            Windows Thai
     MacRoman    cp10000, Roman   Macintosh Roman
     MacLatin2   cp10029          Macintosh Latin2 (Central Europe)
     MacCyrillic cp10007          Macintosh Cyrillic
     MacGreek    cp10006          Macintosh Greek
     MacTurkish  cp10081          Macintosh Turkish
     MacRomanian cp10010          Macintosh Romanian
     MacIceland  cp10079          Macintosh Icelandic
     MacCroatian cp10082          Macintosh Croatian
    
    TYPE may be "FileName" to specify the encoding of file names on the
    command line (ie. FILE arguments). In Windows, this triggers use of
    wide-character i/o routines, thus providing support for Unicode file
    names. See the "WINDOWS UNICODE FILE NAMES" section below for
    details.
