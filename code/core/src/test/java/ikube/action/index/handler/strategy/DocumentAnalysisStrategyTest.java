package ikube.action.index.handler.strategy;

import ikube.AbstractTest;
import ikube.action.index.handler.IStrategy;
import ikube.analytics.IAnalyticsService;
import ikube.model.*;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static ikube.IConstants.*;
import static java.lang.Boolean.TRUE;
import static java.util.Locale.ENGLISH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20-07-2014
 */
public class DocumentAnalysisStrategyTest extends AbstractTest {

    private Document document;
    private List<String> sentences;
    private String sentenceOne = "Hello world.";
    private String sentenceTwo = "My beautiful little girl.";
    private String sentenceThree = "Terrible weather we are having.";

    @Mock
    private Context context;
    @Mock
    private Analysis analysis;
    @Mock
    private IStrategy nextStrategy;
    @Mock
    private IndexableExchange indexableExchange;

    @Mock
    private IAnalyticsService analyticsService;

    @Spy
    @InjectMocks
    private DocumentAnalysisStrategy documentAnalysisStrategy;

    @Before
    public void before() throws Exception {
        document = new Document();
        when(indexableExchange.isVectored()).thenReturn(TRUE);
        when(indexableExchange.isTokenized()).thenReturn(TRUE);
        when(indexableExchange.isAnalyzed()).thenReturn(TRUE);
        when(indexableExchange.isStored()).thenReturn(TRUE);
        when(indexableExchange.getContent()).thenReturn(sentenceOne);

        sentences = newArrayList(sentenceOne, sentenceTwo, sentenceThree);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void aroundProcess() throws Exception {
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return sentences;
            }
        }).when(documentAnalysisStrategy).breakDocumentIntoSentences(anyString(), anyString());
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return POSITIVE;
            }
        }).when(documentAnalysisStrategy).aggregateClassificationForSentences(any(List.class));
        when(nextStrategy.aroundProcess(any(IndexContext.class), any(Indexable.class), any(Document.class), any())).thenReturn(TRUE);

        boolean processed = documentAnalysisStrategy.aroundProcess(indexContext, indexableExchange, document, sentences.toString());
        assertTrue(processed);
        assertEquals(POSITIVE, document.get(CLASSIFICATION));

        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                return NEGATIVE;
            }
        }).when(documentAnalysisStrategy).aggregateClassificationForSentences(any(List.class));
        processed = documentAnalysisStrategy.aroundProcess(indexContext, indexableExchange, document, sentences.toString());
        assertTrue(processed);
        assertEquals(POSITIVE, document.get(CLASSIFICATION));
        assertEquals(NEGATIVE, document.get(CLASSIFICATION_CONFLICT));

        verify(nextStrategy, atLeastOnce()).aroundProcess(any(IndexContext.class), any(Indexable.class), any(Document.class), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void highestVotedClassification() {
        when(analyticsService.analyze(any(Analysis.class))).thenReturn(analysis);
        when(analysis.getClazz()).thenReturn(POSITIVE, NEGATIVE, POSITIVE);
        when(analysis.getOutput()).thenReturn(new double[]{0.63556489, 0.3546825}, new double[]{0.12365489, 0.826554687}, new double[]{0.63556489, 0.3546825});

        String classification = documentAnalysisStrategy.aggregateClassificationForSentences(sentences);
        assertEquals(POSITIVE, classification);

        when(analysis.getClazz()).thenReturn(NEGATIVE, NEGATIVE, POSITIVE);
        when(analysis.getOutput()).thenReturn(new double[]{0.12365489, 0.826554687}, new double[]{0.12365489, 0.826554687}, new double[]{0.63556489, 0.3546825});

        classification = documentAnalysisStrategy.aggregateClassificationForSentences(sentences);
        assertEquals(NEGATIVE, classification);
    }

    @Test
    @Ignore
    public void breakDocumentIntoSentences() throws IOException {
        // This method only works with correct spaces and capitals, i.e. lowercase does not work
        String text = sentenceOne.toLowerCase() + " " + sentenceTwo.toLowerCase() + " " + sentenceThree.toLowerCase();
        String language = ENGLISH.getLanguage();
        List<String> sentences = documentAnalysisStrategy.breakDocumentIntoSentences(text, language);
        for (final String sentence : sentences) {
            logger.error(sentence);
        }
        assertEquals(3, sentences.size());
        assertEquals(sentenceOne, sentences.get(0));
        assertEquals(sentenceTwo, sentences.get(1));
        assertEquals(sentenceThree, sentences.get(2));
    }

    @Test
    public void breakDocumentIntoSentencesSimple() throws IOException {
        String text = sentenceOne.toLowerCase() + " " + sentenceTwo.toLowerCase() + " " + sentenceThree.toLowerCase();
        List<String> sentences = documentAnalysisStrategy.breakDocumentIntoSentences(text);
        for (final String sentence : sentences) {
            logger.error(sentence);
        }
        assertEquals(3, sentences.size());
        assertEquals(sentenceOne.toLowerCase(), sentences.get(0));
        assertEquals(sentenceTwo.toLowerCase(), sentences.get(1));
        assertEquals(sentenceThree.toLowerCase(), sentences.get(2));
    }

}