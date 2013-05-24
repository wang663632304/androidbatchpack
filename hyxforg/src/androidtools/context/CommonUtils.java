package androidtools.context;

import java.io.File;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class CommonUtils {
	private static MessageConsole console = null;
	private static MessageConsoleStream consoleStream = null;
	private static Shell shell = null;

	public static void consolePrint(String message) {
		if (console == null) {
			console = new MessageConsole("MarsorAndroidTools", null);
		}

		ConsolePlugin.getDefault().getConsoleManager()
				.addConsoles(new IConsole[] { console });

		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);

		if (consoleStream == null) {
			consoleStream = console.newMessageStream();
		}
		consoleStream.setEncoding("UTF-8");

		console.newMessageStream().println(message);
	}

	public static void messageBox(String message) {
		try {
			if (shell == null) {
				shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getShell();
			}

			if (shell != null) {
				MessageDialog.openInformation(shell, "兰贝壳儿", message);
				return;
			}
			consolePrint(message);
		} catch (Exception localException) {
		}
	}

	public static String fixFile(String[] pathSections) {
		String[] arrayOfString;
		if ((pathSections == null) || (pathSections.length == 0))
			return null;

		String result = "";
		int j = (arrayOfString = pathSections).length;
		for (int i = 0; i < j; ++i) {
			String str = arrayOfString[i];
			if (!(str.endsWith(File.separator))){
				result = result + str + File.separatorChar;
			}
			else{
				result = result + str;
			}
		}

		result = result.replaceAll("(.*?).$", "$1");
		return result;
	}
}