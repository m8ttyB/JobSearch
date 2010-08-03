package com.freecog.craigslist.unittests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.freecog.craigslist.CraigListMain;

public class TestCraigsListMain {

	@Test
	public void testDirExists() {
		String dir = "/Users/";
		assertTrue(CraigListMain.checkDirExists(dir));
	}
	
	@Test
	public void testDirNotExists() {
		String dir = "tunafish/";
		assertFalse(CraigListMain.checkDirExists(dir));
	}
	
	@Test 
	public void testOneArg() {
		String arg = "qa";
		String[] args = CraigListMain.getArgs(arg);
		assertTrue( arg.equals(args[0]));
	}
	
	@Test
	public void testTwoArgs() {
		String args = "qa, software tester ";
		String expected1 = "qa";
		String expected2 = "software tester";
		String[] actual = CraigListMain.getArgs(args);
		assertTrue( expected1.equals( actual[0]));
		assertTrue( expected2.equals( actual[1] ));
	}

}
