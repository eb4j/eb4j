package io.github.eb4j;

import org.testng.annotations.Test;

import io.github.eb4j.hook.Hook;
import io.github.eb4j.hook.DefaultHook;

import java.io.File;

import static org.testng.Assert.*;

/**
 * Created by miurahr on 16/06/05.
 */
public class SubBookTest {

    Book book;
    SubBook[] subbooks;

    @Test(groups = "init")
    public void testSubBookConstructor() throws Exception {
        File bookPath = new File(this.getClass().getResource("/data/epwing").getFile());
        File appendixPath = null;
        book = new Book(bookPath, appendixPath);
        subbooks = book.getSubBooks();
        assertNotNull(subbooks, "Subbook constructor should not return null.");
    }


    @Test(dependsOnGroups = {"init"})
    public void testGetBook() throws Exception {
        assertEquals(subbooks[0].getBook(), book, "getBook() returns parent book.");
    }

    @Test(dependsOnGroups = {"init"})
    public void testSetAppendix() throws Exception {
        // TODO: need test data
    }

    @Test(dependsOnGroups = {"init"})
    public void testGetSubAppendix() throws Exception {
        // TODO: need test data
    }

    @Test(dependsOnGroups = {"init"})
    public void testGetTitle() throws Exception {
        assertEquals(subbooks[0].getTitle(), "\uFF34\uFF25\uFF33\uFF34"); // TEST in zenkaku
    }

    @Test(dependsOnGroups = {"init"})
    public void testGetName() throws Exception {
        assertEquals(subbooks[0].getName(), "test");
    }

    @Test(dependsOnGroups = {"init"})
    public void testGetFont() throws Exception {
        // TODO: need test data
        // this method returns GAIJI font.
    }

    @Test(dependsOnGroups = {"init"})
    public void testGetFont1() throws Exception {
        // TODO: need test data
        // this method returns GAIJI font.
    }

    @Test(dependsOnGroups = {"init"})
    public void testSetFont() throws Exception {
        subbooks[0] .setFont(ExtFont.FONT_48);
    }

    @Test(dependsOnGroups = {"init"})
    public void testGetGraphicData() throws Exception {

    }

    @Test
    public void testGetSoundData() throws Exception {

    }

    @Test
    public void testGetTextFile() throws Exception {

    }

    @Test
    public void testGetGraphicFile() throws Exception {

    }

    @Test
    public void testGetSoundFile() throws Exception {

    }

    @Test
    public void testGetMovieFileList() throws Exception {

    }

    @Test
    public void testGetMovieFile() throws Exception {

    }

    @Test
    public void testGetHeading() throws Exception {

    }

    @Test
    public void testGetNextHeadingPosition() throws Exception {

    }

    @Test
    public void testGetText() throws Exception {

    }

    @Test
    public void testGetMenu() throws Exception {

    }

    @Test
    public void testGetImageMenu() throws Exception {

    }

    @Test
    public void testGetCopyright() throws Exception {

    }

    @Test
    public void testGetWordIndexStyle() throws Exception {

    }

    @Test
    public void testGetEndwordIndexStyle() throws Exception {
        // TODO need test data.
    }

    @Test(dependsOnGroups = {"init"})
    public void testSearchExactword() throws Exception {
        // TODO need test data.
    }

    @Test(dependsOnGroups = {"init"})
    public void testSearchWord() throws Exception {
        assertTrue(subbooks[1].hasWordSearch());
        Hook<String> hook = new DefaultHook(subbooks[1]);
        Searcher sh = subbooks[1].searchWord("Tokyo");
        Result sr;
        String article;
        Boolean res = false;
        while ((sr = sh.getNextResult()) != null) {
            article = sr.getText(hook);
            assertEquals(article, "Tokyo\n\u6771\u4eac\n");
            res = true;
        }
        assertTrue(res, "Fail to search word.");
    }

    @Test
    public void testSearchEndword() throws Exception {
        // TODO need test data.
    }

    @Test
    public void testSearchKeyword() throws Exception {
        // TODO need test data.
    }

    @Test
    public void testSearchCross() throws Exception {
        // TODO need test data.
    }

    @Test
    public void testSearchMulti() throws Exception {
        // TODO need test data.
    }

    @Test(dependsOnGroups = {"init"})
    public void testHasMenu() throws Exception {
        assertFalse(subbooks[0].hasMenu());
    }

    @Test(dependsOnGroups = {"init"})
    public void testHasImageMenu() throws Exception {
        assertFalse(subbooks[0].hasImageMenu());
    }

    @Test(dependsOnGroups = {"init"})
    public void testHasCopyright() throws Exception {
        assertFalse(subbooks[0].hasCopyright());
    }

    @Test(dependsOnGroups = {"init"})
    public void testHasExactwordSearch() throws Exception {
        assertTrue(subbooks[0].hasExactwordSearch());
    }

    @Test(dependsOnGroups = {"init"})
    public void testHasWordSearch() throws Exception {
        assertTrue(subbooks[0].hasWordSearch());
    }

    @Test(dependsOnGroups = {"init"})
    public void testHasEndwordSearch() throws Exception {
        assertFalse(subbooks[0].hasEndwordSearch());
    }

    @Test(dependsOnGroups = {"init"})
    public void testHasKeywordSearch() throws Exception {
        assertFalse(subbooks[0].hasKeywordSearch());
    }

    @Test(dependsOnGroups = {"init"})
    public void testHasCrossSearch() throws Exception {
        assertFalse(subbooks[0].hasCrossSearch());
    }

    @Test(dependsOnGroups = {"init"})
    public void testHasMultiSearch() throws Exception {
        assertFalse(subbooks[0].hasMultiSearch());
    }

    @Test(dependsOnGroups = {"init"})
    public void testGetMultiCount() throws Exception {

    }

    @Test(dependsOnGroups = {"init"})
    public void testGetMultiTitle() throws Exception {

    }

    @Test(dependsOnGroups = {"init"})
    public void testGetMultiEntryCount() throws Exception {

    }

    @Test(dependsOnGroups = {"init"})
    public void testGetMultiEntryLabel() throws Exception {

    }

    @Test(dependsOnGroups = {"init"})
    public void testGetCandidate() throws Exception {

    }

    @Test
    public void testHasMultiEntryCandidate() throws Exception {

    }

    @Test(dependsOnGroups = {"init"})
    public void testToString() throws Exception {
        assertEquals(subbooks[0].toString(), subbooks[0].getTitle());
    }

}