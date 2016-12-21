package com.zsm.pocketlamb;

import java.util.Arrays;
import java.util.Hashtable;

public class MorseCode {

	public final static int DOT = 1;
	public final static int LINE = 3;
	public final static int INNER_GAP = 2;
	public final static int OUTER_GAP = 7;
	
	final static private String MORSE_CODE[][]
	= { {"A",".-"}, {"B","-..."}, {"C","-.-."}, {"D","-.."}, {"E","."}, {"F","..-."}, 
		{"G","--."}, {"H","...."}, {"I",".."}, {"J",".---"}, {"K","-.-"}, {"L",".-.."}, 
		{"M","--"}, {"N","-."}, {"O","---"}, {"P",".--."}, {"Q","--.-"}, {"R",".-."}, 
		{"S","..."}, {"T","-"}, {"U","..-"}, {"V","...-"}, {"W",".--"}, {"X","-..-"}, 
		{"Y","-.--"}, {"Z","--.."},
		{"1",".----"}, {"2","..---"}, {"3","...--"}, {"4","....-"}, {"5","....."}, 
		{"6","-...."}, {"7","--..."}, {"8","---.."}, {"9","----."}, {"0","-----"}, 
		{"?","..--.."}, {"/","-..-."}, {"{","-.-.."}, {"}, ",".---."}, {"-","-....-"}, 
		{".",".-.-.-"}, {"@","--.-."}, {"*","----"}, {"$","...-."}, {"#","..--"},  };
	static private final Hashtable<Character, int[]> WORD_TO_MORSE
		= new Hashtable<Character, int[]>( MORSE_CODE.length );
		
	static {
		for( String[] item : MORSE_CODE ) {
			WORD_TO_MORSE.put( item[0].charAt(0), toInnerMorseCode(item[1]) );
		}
	}
	
	static private int[] toInnerMorseCode( String code )  {
		int len = code.length();
		int[] innerCode = new int[len*2];
		
		for( int i = 0; i < len; i++ ) {
			innerCode[i*2] = (code.charAt(i) == '.' ? DOT : LINE);
			innerCode[i*2+1] = INNER_GAP;
		}
		innerCode[2*len-1] = OUTER_GAP;
		
		return innerCode;
	}
	
	static public int[] toMorseCode( char c ) {
		return WORD_TO_MORSE.get(Character.toUpperCase(c));
	}
	
	static public int[] toMorseCode( String word ) {
		char[] c = toCharArray( word );
		int len = 0;
		for( int i = 0; i < c.length; i++ ) {
			len +=  WORD_TO_MORSE.get(c[i]).length;
		}
		
		int[] code = new int[len];
		int index = 0;
		for( int i = 0; i < c.length; i++ ) {
			int[] mc =  WORD_TO_MORSE.get(c[i]);
			for( int j = 0; j < mc.length; j++ ) {
				code[index++] = mc[j];
			}
		}
		
		return code;
	}
	
	static public char[] toCharArray( String word ) {
		char[] a = new char[word.length()];
		int index = 0;
		for( int i = 0; i < a.length; i++ ) {
			char c = Character.toUpperCase( word.charAt(i) );
			if( WORD_TO_MORSE.containsKey( c ) ) {
				a[index++] = c;
			}
		}
		
		return Arrays.copyOf(a, index);
	}
	
}
