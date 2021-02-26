package io.github.eb4j;

import io.github.eb4j.io.EBFormat;

/**
 * ifeval::["{lang}" == "en"]
 * EBook main data files.
 *
 * endif::[]
 * Created by Hiroshi Miura on 16/07/01.
 * @author Hiroshi Miura
 */
class DataFiles {

    private String honmonFileName;
    private EBFormat honmonFormat;
    private String graphicFileName;
    private EBFormat graphicFormat;
    private String soundFileName;
    private EBFormat soundFormat;

    DataFiles(final String honmonFileName, final EBFormat honmonFormat,
                     final String graphicFileName, final EBFormat graphicFormat,
                     final String soundFileName, final EBFormat soundFormat) {
        this.honmonFileName = honmonFileName;
        this.honmonFormat = honmonFormat;
        this.graphicFileName = graphicFileName;
        this.graphicFormat = graphicFormat;
        this.soundFileName = soundFileName;
        this.soundFormat = soundFormat;
    }

    DataFiles(final String filename, final EBFormat format) {
        this.honmonFileName = filename;
        this.honmonFormat = format;
    }

    void setHonmon(final String filename, final EBFormat format) {
        this.honmonFileName = filename;
        this.honmonFormat = format;
    }

    String getHonmon() {
        return honmonFileName;
    }

    EBFormat getHonmonFormat() {
        return honmonFormat;
    }

    void setGraphic(final String filename, final EBFormat format) {
        this.graphicFileName = filename;
        this.graphicFormat = format;
    }

    String getGraphic() {
        return graphicFileName;
    }

    EBFormat getGraphicFormat() {
        return graphicFormat;
    }

    boolean hasGraphic() {
        return graphicFileName != null;
    }

    void setSound(final String filename, final EBFormat format) {
        this.soundFileName = filename;
        this.soundFormat = format;
     }

    String getSound() {
        return soundFileName;
    }

    EBFormat getSoundFormat() {
        return soundFormat;
    }

    boolean hasSound() {
        return soundFileName != null;
    }
}
