package hu.ben.photoalbumorganizer.model;

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
public class MediaWrapper {

    private ArrayList<MediaDirectory> mediaDirectoryList = new ArrayList<>();

    public int getNumberOfFiles() {
        int result = 0;
        for (MediaDirectory md : mediaDirectoryList) {
            result += md.getImageFileWrapperList().size() + md.getVideoFileWrapperList().size();
        }
        return result;
    }

}
