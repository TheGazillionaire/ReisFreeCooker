package cc.fish.rfl;

import cc.fish.rfl.api.reis.ReisCooker;
import cc.fish.rfl.api.reis.ReisDistributor;
import cc.fish.rfl.api.reis.ReisUpdater;
import cc.fish.rfl.api.utils.ConsoleUtil;
import cc.fish.rfl.api.utils.EncryptionUtil;
import cc.fish.rfl.api.utils.JavaUtil;
import java.util.Base64;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RflMain {
    private static RflMain instance;

    public static final Logger LOGGER = LogManager.getLogger("RFL");

    public void start(CommandLine commandLine) {
        boolean standalone = commandLine.hasOption("standalone");
        boolean help = commandLine.hasOption("help");

        // print help message
        if (help) {
            new HelpFormatter().printHelp("Reis Free Cooker", createOptions());
            return;
        }

        // only start Reis distributor if standalone mode is enabled
        if (standalone) {
            LOGGER.info("Running in standalone mode, only starting Reis Distribution...");
            new ReisDistributor().startServer(commandLine.hasOption("debug-packets"));
            return;
        }

        // für cool und so
        ConsoleUtil.clearConsole();
        ConsoleUtil.emptyLine();

        System.out.println(ConsoleUtil.getCustomArt("REIS FREE COOKER", null));

        LOGGER.info("Welcome to Reis Free Cooker!");
        ConsoleUtil.emptyLine();

        // get proper java version
        String javaCommand = commandLine.hasOption("with-java-path")
            ? commandLine.getOptionValue("with-java-path")
            : JavaUtil.findProperJava();

        // add option to disable auto updates in case this has been patched.
        ReisUpdater.checkAndUpdate(commandLine.hasOption("no-update"));
        ConsoleUtil.emptyLine();

        LOGGER.info("Farming Reis for Free...");
        new Thread(() -> ReisCooker.cook(javaCommand, commandLine.hasOption("enable-mc-output")), "Reis").start();

        LOGGER.info("Lying to Reis Distributor...");
        new ReisDistributor().startServer(commandLine.hasOption("debug-packets"));

        LOGGER.info("Thank you for using Reis Free Cooker!");
    }

    public static void main(String[] args) {
        Options options = createOptions();

        try {
            getInstance().start(new DefaultParser().parse(options, args));
        } catch (ParseException e) {
            new HelpFormatter().printHelp("Reis Free Cooker", options);
        }
    }

    // commons-cli
    private static Options createOptions() {
        Options options = new Options();

        {
            options.addOption(Option.builder()
                .longOpt("enable-mc-output")
                .desc("Enables " + EncryptionUtil.decrypt("HwwHFigXGDQR", "ReisKey") + " output in the console.").build());

            options.addOption(Option.builder()
                .longOpt("debug-packets")
                .desc("Enables packet debugging for distribution purposes.").build());

            options.addOption(Option.builder()
                .longOpt("no-update")
                .desc("Disables automatic updates for Reis Farmer.").build());

            options.addOption(Option.builder()
                .longOpt("with-java-path").hasArg()
                .argName("java-path")
                .desc("Specifies the path to the Java executable to use.").build());

            // for developer purposes i guess idk
            options.addOption(Option.builder()
                .longOpt("standalone")
                .desc("Doesn't cook Reis, but instead only starts the Reis Distributor.").build());

            options.addOption(Option.builder()
                .longOpt("help")
                .desc("Displays this help message.").build());
        }

        return options;
    }

    public static RflMain getInstance() {
        if (instance == null) {
            instance = new RflMain();
        }
        return instance;
    }
}
