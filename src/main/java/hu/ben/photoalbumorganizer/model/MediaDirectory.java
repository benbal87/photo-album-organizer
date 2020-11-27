package hu.ben.photoalbumorganizer.model;

import java.io.File;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MediaDirectory {

    private File directoryFile;

    private String latestCreationDate;

    private ArrayList<ImageFileWrapper> imageFileWrapperList = new ArrayList<>();

    private ArrayList<VideoFileWrapper> videoFileWrapperList = new ArrayList<>();

}
