### Photo Album Organizer:

This is a mini project with the goal to rename all image and video files in one directory and all of its subdirectories 
and correcting dates of video files.
The directories which containing the media files have to be named customized. 
* The names of these directories will be the root names of the media files.
* Then the media file with the latest creation date in a directory will be the reference for the start of the media file names with the following format: "YYYY-MM-DD".
* The files then will be numbered with the following format if there are not more than 999 in a directory: 001, 002, etc.
* The video files will get a "video" string before their numbers.
* All the files will be numbered by their creation dates.

After the renaming of the files has been finished the handbrake converting of the video files will start.
* It will convert the video files with the quality of 28.
* All the handbraked video files will get a "__HB" string added to their file names.
* The original video files would not be deleted.

After the handbrake process has been finished the exiftool will correct the creation dates of the handbraked videos.
