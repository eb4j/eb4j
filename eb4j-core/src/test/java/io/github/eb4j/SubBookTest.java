package io.github.eb4j;

import org.testng.annotations.Test;

import io.github.eb4j.hook.Hook;
import io.github.eb4j.hook.DefaultHook;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Created by miurahr on 16/06/05.
 */
public class SubBookTest {

    private Book book;
    private SubBook[] subbooks;

    @Test(groups = "init")
    void testSubBookConstructor() throws Exception {
        File bookPath = new File(this.getClass().getResource("/data/epwing").getFile());
        File appendixPath = null;
        book = new Book(bookPath, appendixPath);
        subbooks = book.getSubBooks();
        assertNotNull(subbooks, "Subbook constructor should not return null.");
    }


    @Test(dependsOnGroups = {"init"})
    void testGetBook() {
        assertEquals(subbooks[0].getBook(), book, "getBook() returns parent book.");
    }

    @Test(dependsOnGroups = {"init"})
    void testSetAppendix() {
        // TODO: need test data
    }

    @Test(dependsOnGroups = {"init"})
    void testGetSubAppendix() {
        // TODO: need test data
    }

    @Test(dependsOnGroups = {"init"})
    void testGetTitle() {
        assertEquals(subbooks[0].getTitle(), "\uFF34\uFF25\uFF33\uFF34"); // TEST in zenkaku
    }

    @Test(dependsOnGroups = {"init"})
    void testGetName() {
        assertEquals(subbooks[0].getName(), "test");
    }

    @Test(dependsOnGroups = {"init"})
    void testGetFont() {
        // TODO: need test data
        // this method returns GAIJI font.
    }

    @Test(dependsOnGroups = {"init"})
    void testGetFont1() {
        // TODO: need test data
        // this method returns GAIJI font.
    }

    @Test(dependsOnGroups = {"init"})
    void testSetFont() {
        subbooks[0] .setFont(ExtFont.FONT_48);
    }

    @Test(dependsOnGroups = {"init"})
    void testGetGraphicData() {

    }

    @Test
    void testGetSoundData() {

    }

    @Test
    void testGetTextFile() {

    }

    @Test
    void testGetGraphicFile() {

    }

    @Test
    void testGetSoundFile() {

    }

    @Test
    void testGetMovieFileList() {

    }

    @Test
    void testGetMovieFile() {

    }

    @Test
    void testGetHeading() {

    }

    @Test
    void testGetNextHeadingPosition() {

    }

    @Test
    void testGetText() {

    }

    @Test
    void testGetMenu() {

    }

    @Test
    void testGetImageMenu() {

    }

    @Test
    void testGetCopyright() {

    }

    @Test
    void testGetWordIndexStyle() {

    }

    @Test
    void testGetEndwordIndexStyle() {
        // TODO need test data.
    }

    @Test(dependsOnGroups = {"init"})
    void testSearchExactword() {
        // TODO need test data.
    }

    @Test(dependsOnGroups = {"init"})
    void testSearchWord() throws Exception {
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
    void testSearchEndword() {
        // TODO need test data.
    }

    @Test
    void testSearchKeyword() {
        // TODO need test data.
    }

    @Test
    void testSearchCross() {
        // TODO need test data.
    }

    @Test
    void testSearchMulti() {
        // TODO need test data.
    }

    @Test(dependsOnGroups = {"init"})
    void testHasMenu() {
        assertFalse(subbooks[0].hasMenu());
    }

    @Test(dependsOnGroups = {"init"})
    void testHasImageMenu() {
        assertFalse(subbooks[0].hasImageMenu());
    }

    @Test(dependsOnGroups = {"init"})
    void testHasCopyright() {
        assertFalse(subbooks[0].hasCopyright());
    }

    @Test(dependsOnGroups = {"init"})
    void testHasExactwordSearch() throws Exception {
        assertTrue(subbooks[0].hasExactwordSearch());
    }

    @Test(dependsOnGroups = {"init"})
    void testHasWordSearch() throws Exception {
        assertTrue(subbooks[0].hasWordSearch());
    }

    @Test(dependsOnGroups = {"init"})
    void testHasEndwordSearch() throws Exception {
        assertFalse(subbooks[0].hasEndwordSearch());
    }

    @Test(dependsOnGroups = {"init"})
    void testHasKeywordSearch() throws Exception {
        assertFalse(subbooks[0].hasKeywordSearch());
    }

    @Test(dependsOnGroups = {"init"})
    void testHasCrossSearch() {
        assertFalse(subbooks[0].hasCrossSearch());
    }

    @Test(dependsOnGroups = {"init"})
    void testHasMultiSearch() {
        assertFalse(subbooks[0].hasMultiSearch());
    }

    @Test(dependsOnGroups = {"init"})
    void testGetMultiCount() {

    }

    @Test(dependsOnGroups = {"init"})
    void testGetMultiTitle() {

    }

    @Test(dependsOnGroups = {"init"})
    void testGetMultiEntryCount() {

    }

    @Test(dependsOnGroups = {"init"})
    void testGetMultiEntryLabel() {

    }

    @Test(dependsOnGroups = {"init"})
    void testGetCandidate() {

    }

    @Test
    void testHasMultiEntryCandidate() {

    }

    @Test(dependsOnGroups = {"init"})
    void testToString() {
        assertEquals(subbooks[0].toString(), subbooks[0].getTitle());
    }

}
