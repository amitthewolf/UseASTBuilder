/*
 * USE - UML based specification environment
 * Copyright (C) 1999-2004 Mark Richters, University of Bremen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

// $Id: Main.java 5702 2015-05-28 10:24:31Z fhilken $

package org.tzi.use.main;

import java.awt.Font;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Path;

import javax.swing.ImageIcon;
import javax.swing.UIDefaults;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.tzi.use.config.Options;
import org.tzi.use.main.runtime.IRuntime;
import org.tzi.use.main.shell.Shell;
import org.tzi.use.parser.ocl.ASTExpression;
import org.tzi.use.parser.ocl.OCLCompiler;
import org.tzi.use.parser.use.USECompiler;
import org.tzi.use.uml.mm.*;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.extension.ExtensionManager;
import org.tzi.use.uml.ocl.type.EnumType;
import org.tzi.use.uml.ocl.type.IntegerType;
import org.tzi.use.uml.ocl.type.Type;
import org.tzi.use.uml.ocl.type.TypeFactory;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.util.Log;
import org.tzi.use.util.USEWriter;

/**
 * Main class.
 * 
 * @version $ProjectVersion: 0.393 $
 * @author Mark Richters
 */
public final class Main {

	// utility class
	private Main() {
	}

	private static void initGUIdefaults() {
		MetalLookAndFeel.setCurrentTheme(new MyTheme());
	}

	public static void main(String args[]) throws MInvalidModelException, IOException {

		// Test 1
//		String input = "self.employee->exists(e | e.age > 45))";
//////		String input = "Employee.allInstances()->forAll(e | e <> self implies e.name <> self.name)";
//		input = input.replace(".","SEPERATOR");
//		input = input.replace("null","NULL");
//		input = input.replace("->","POINTER");
//		input = input.replace("oclIsKindOf","IsType");
//		input = input.replace("()","");
//		ParseAndPrint(input);

		// Text File
		String Filename = "C:/Users/Amit Wolf/Documents/GitHub/UpdatedOCLRec/OCLRecommendation/ValidExpressions.txt";
		TextToText(Filename);

		//////////////////////////////////////////////// Args to AST - Jar

//		String input = args[0];
//		Expression expr = OCLCompiler.compileExpression(mModel, input,
//				"USE Api", errorPrinter, system.varBindings());
//		System.out.println(expr.toString());
	}

	private static String CleanupAST(String AST)
	{
		String[] Terms = AST.split(" ");
		String NewString = "";
		for (String Term:Terms) {
			if(!Term.contains("org.tzi.use"))
				NewString = NewString+Term;
		}
		return NewString;
	}

	private static void ParseAndPrint(String Input) throws MInvalidModelException {
		ModelFactory mFactory = new ModelFactory();
		MModel mModel = mFactory.createModel("unnamed");
		MAssociationClass PersonClass = mFactory.createAssociationClass("Person",false);
		MClass FriendClass = mFactory.createClass("Friend",false);
		MAssociation NewAss = mFactory.createAssociation("Friendship");
		MMultiplicity multi = mFactory.createMultiplicity();
		MAssociationEnd AssEnd = mFactory.createAssociationEnd(FriendClass,"Pal",multi,1,true,null);
		PersonClass.addAssociationEnd(AssEnd);
		MAttribute newAtt = mFactory.createAttribute("Age",TypeFactory.mkInteger());
		mModel.addClass(PersonClass);
		mModel.addClass(FriendClass);
		FriendClass.addAttribute(newAtt);
//		System.out.println(PersonClass.associationEnds());
		MSystem system = new MSystem(mModel);
		PrintWriter errorPrinter = new PrintWriter(new StringWriter(), true);
		ASTExpression out = OCLCompiler.compileExpressionAST(mModel, Input,"USE Api", errorPrinter, system.varBindings());

		System.out.println(out);
	}

	private static void TextToText(String FileName) throws MInvalidModelException, IOException {
		ModelFactory mFactory = new ModelFactory();
		MModel mModel = mFactory.createModel("unnamed");
		MClass NewClass = mFactory.createClass("Person",false);
		MAttribute newAtt = mFactory.createAttribute("Age",TypeFactory.mkInteger());
		mModel.addClass(NewClass);
		NewClass.addAttribute(newAtt);
		MSystem system = new MSystem(mModel);
		BufferedReader reader = new BufferedReader(new FileReader(FileName));
		File ASTFile = new File("ValidAST.txt");
		if (ASTFile.createNewFile()) {
			System.out.println("File created: " + ASTFile.getName());
		} else {
			System.out.println("File already exists.");
		}
		FileWriter AstWriter = new FileWriter("ValidAST.txt");
		String Line = reader.readLine();
		boolean First = true;
		String ID = "";
		String Expression = "";
		ASTExpression AST = null;
		while (Line != null)
		{
			if(First)
			{
				String[] SplitLine = Line.substring(1).split("#");
				ID = SplitLine[0];
				Expression = SplitLine[1];
				First = false;
			}
			else if(Line.length()>0 && Line.charAt(0)=='@')
			{
				// Add Last Line
				PrintWriter errorPrinter = new PrintWriter(new StringWriter(), true);
				Expression = Expression.replace(".","SEPERATOR");
				Expression = Expression.replace("null","NULL");
				Expression = Expression.replace("->","POINTER");
				Expression = Expression.replace("oclIsKindOf","IsType");
				Expression = Expression.replace("()","");
				AST = OCLCompiler.compileExpressionAST(mModel, Expression,
						"USE Api", errorPrinter, system.varBindings());
				String ASTtoString = null;
				try
				{
					ASTtoString = AST.toString();
//					ASTtoString = CleanupAST(ASTtoString);
				}
				catch (Exception e)
				{
					System.out.println("Original = "+Line);
					System.out.println(Expression);
				}
				AstWriter.write(ID+"$"+ASTtoString+"\n");
				Expression = "";
				ID = "";
				String[] SplitLine = Line.substring(1).split("#");
				try
				{
					ID = SplitLine[0];
					Expression = SplitLine[1].replaceAll(System.getProperty("line.separator"), "");
				}
				catch (Exception e)
				{
					System.out.println("Original = "+Line);
					System.out.println("Splitted = "+SplitLine);
				}

			}
			else
			{
				Expression = Expression + Line;
				Expression = Expression.replaceAll(System.getProperty("line.separator"), "");
			}
			Line = reader.readLine();
		}
		PrintWriter errorPrinter = new PrintWriter(new StringWriter(), true);
		AST = OCLCompiler.compileExpressionAST(mModel, Expression,
				"USE Api", errorPrinter, system.varBindings());
		AstWriter.write(ID+"$"+AST+"\n");
		reader.close();
		AstWriter.close();
	}
//
//	public static void main(String args[]) {
//		// set System.out to the OldUSEWriter to protocol the output.
//		System.setOut(USEWriter.getInstance().getOut());
//		// set System.err to the OldUSEWriter to protocol the output.
//		System.setErr(USEWriter.getInstance().getErr());
//
//		// read and set global options, setup application properties
//		Options.processArgs(args);
//		if (Options.doGUI) {
//			initGUIdefaults();
//		}
//
//		Session session = new Session();
//		IRuntime pluginRuntime = null;
//		MModel model = null;
//		MSystem system = null;
//
//		if (!Options.disableExtensions) {
//			ExtensionManager.EXTENSIONS_FOLDER = Options.homeDir + "/oclextensions";
//			ExtensionManager.getInstance().loadExtensions();
//		}
//
//		// Plugin Framework
//		if (Options.doPLUGIN) {
//			// create URL from plugin directory
//			Path pluginDirURL = Options.pluginDir;
//			Log.verbose("Plugin path: [" + pluginDirURL + "]");
//			Class<?> mainPluginRuntimeClass = null;
//			try {
//				mainPluginRuntimeClass = Class
//						.forName("org.tzi.use.runtime.MainPluginRuntime");
//			} catch (ClassNotFoundException e) {
//				Log
//						.error("Could not load PluginRuntime. Probably use-runtime-...jar is missing.\n"
//								+ "Try starting use with -noplugins switch.\n"
//								+ e.getMessage());
//				System.exit(1);
//			}
//			try {
//				Method run = mainPluginRuntimeClass.getMethod("run",
//						new Class[] { Path.class });
//				pluginRuntime = (IRuntime) run.invoke(null,
//						new Object[] { pluginDirURL });
//				Log.debug("Starting plugin runtime, got class ["
//						+ pluginRuntime.getClass() + "]");
//			} catch (Exception e) {
//				e.printStackTrace();
//				Log.error("FATAL ERROR.");
//				System.exit(1);
//			}
//		}
//
//		// compile spec if filename given as argument
//		if (Options.specFilename != null) {
//			try (FileInputStream specStream = new FileInputStream(Options.specFilename)){
//				Log.verbose("compiling specification...");
//				model = USECompiler.compileSpecification(specStream,
//						Options.specFilename, new PrintWriter(System.err),
//						new ModelFactory());
//			} catch (FileNotFoundException e) {
//				Log.error("File `" + Options.specFilename + "' not found.");
//				System.exit(1);
//			} catch (IOException e1) {
//				// close failed
//			}
//
//			// compile errors?
//			if (model == null) {
//				System.exit(1);
//			}
//
//			if(!Options.quiet){
//				Options.setLastDirectory(new java.io.File(Options.specFilename).getAbsoluteFile().toPath().getParent());
//			}
//			if (!Options.testMode)
//				Options.getRecentFiles().push(Options.specFilename);
//
//			if (Options.compileOnly) {
//				Log.verbose("no errors.");
//				if (Options.compileAndPrint) {
//					MMVisitor v = new MMPrintVisitor(new PrintWriter(
//							System.out, true));
//					model.processWithVisitor(v);
//				}
//				System.exit(0);
//			}
//
//			// print some info about model
//			Log.verbose(model.getStats());
//
//			// create system
//			system = new MSystem(model);
//		}
//		session.setSystem(system);
//
//		if (Options.doGUI) {
//			Class<?> mainWindowClass = null;
//			try {
//				mainWindowClass = Class
//						.forName("org.tzi.use.gui.main.MainWindow");
//				Log.debug("Initializing [" + mainWindowClass.toString() + "]");
//			} catch (ClassNotFoundException e) {
//				Log
//						.error("Could not load GUI. Probably use-gui-...jar is missing.\n"
//								+ "Try starting use with -nogui switch.\n" + e);
//				System.exit(1);
//			}
//			if (mainWindowClass == null) {
//				Log.error("MainWindow could not be initialized! Exiting!");
//				System.exit(1);
//			}
//			try {
//				if (pluginRuntime == null) {
//					Log.debug("Starting gui without plugin runtime!");
//					Method create = mainWindowClass.getMethod("create",
//							new Class[] { Session.class });
//					Log.debug("Invoking method create with ["
//							+ session.toString() + "]");
//					create.invoke(null, new Object[] { session });
//				} else {
//					Log.debug("Starting gui with plugin runtime.");
//					Method create = mainWindowClass.getMethod("create",
//							new Class[] { Session.class, IRuntime.class });
//					Log.debug("Invoking method create with ["
//							+ session.toString() + "] ["
//							+ pluginRuntime.toString() + "]");
//					create
//							.invoke(null,
//									new Object[] { session, pluginRuntime });
//				}
//			} catch (Exception e) {
//				Log.error("FATAL ERROR.", e);
//				System.exit(1);
//			}
//		}
//
//		// create thread for shell
//		Shell.createInstance(session, pluginRuntime);
//		Shell sh = Shell.getInstance();
//		Thread t = new Thread(sh);
//		t.start();
//
//		// wait on exit from shell (this thread never returns)
//		try {
//			t.join();
//		} catch (InterruptedException ex) {
//			// ignored
//		}
//	}
//
}

/**
 * A theme with full control over fonts and customized tree display.
 */
class MyTheme extends DefaultMetalTheme {
	private FontUIResource controlFont;

	private FontUIResource systemFont;

	private FontUIResource userFont;

	private FontUIResource smallFont;

	MyTheme() {
		// System.out.println("font: " + Font.getFont("use.gui.controlFont"));
		controlFont = new FontUIResource(Font.getFont("use.gui.controlFont",
				super.getControlTextFont()));
		systemFont = new FontUIResource(Font.getFont("use.gui.systemFont",
				super.getSystemTextFont()));
		userFont = new FontUIResource(Font.getFont("use.gui.userFont", super
				.getUserTextFont()));
		smallFont = new FontUIResource(Font.getFont("use.gui.smallFont", super
				.getSubTextFont()));
	}

	public String getName() {
		return "USE";
	}

	public FontUIResource getControlTextFont() {
		return controlFont;
	}

	public FontUIResource getSystemTextFont() {
		return systemFont;
	}

	public FontUIResource getUserTextFont() {
		return userFont;
	}

	public FontUIResource getMenuTextFont() {
		return controlFont;
	}

	public FontUIResource getWindowTitleFont() {
		return controlFont;
	}

	public FontUIResource getSubTextFont() {
		return smallFont;
	}

	public void addCustomEntriesToTable(UIDefaults table) {
		initIcon(table, "Tree.expandedIcon", "TreeExpanded.gif");
		initIcon(table, "Tree.collapsedIcon", "TreeCollapsed.gif");
		initIcon(table, "Tree.leafIcon", "TreeLeaf.gif");
		initIcon(table, "Tree.openIcon", "TreeOpen.gif");
		initIcon(table, "Tree.closedIcon", "TreeClosed.gif");
		table.put("Desktop.background", table.get("Menu.background"));
	}

	private void initIcon(UIDefaults table, String property, String iconFilename) {
		table.put(property, new ImageIcon(Options.getIconPath(iconFilename).toString()));
	}

}
