package WintermuteTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import Wintermute.Wintermute;

public class WintermuteTests {

	Wintermute bot = new Wintermute();

	@Test
	public void testContainsIm() {
		String[] testStrings = { "i'm", "im", "i am", "l'm", "lm", "l am" };
		String[] testSentences = { "This shouldn't do anything.", 
				"I am a test.", "Im a test.", "I'm a test.", "l am a test.", "lm a test.", "l'm a test.", 
				"I am a multi-sentence test. This is the other sentence.",
				"I am I am a test that should fail.", "I'm im a failure, too.",
				"Im i'm a multi-sentence failure! This is the other sentence.", "Improvisation"};
		String[] testExpectedResults = {null, 
				"i am", "im", "i'm",
				"l am", "lm", "l'm",
				"i am",
				null, null,
				null, null};
		
		for (int i = 0; i < testSentences.length; i++) {
			 assertEquals(testExpectedResults[i], bot.containsIm(testSentences[i].toLowerCase(), testStrings));
		}
	}
}
