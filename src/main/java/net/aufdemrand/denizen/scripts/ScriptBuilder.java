package net.aufdemrand.denizen.scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.scripts.commands.core.EngageCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.DebugElement;

public class ScriptBuilder {

    Denizen plugin;
    Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

    public ScriptBuilder(Denizen denizenPlugin) {
        plugin = denizenPlugin;
    }

    /* 
     * Builds and argument array, recognizing items in quotes as a single item 
     */

    public String[] buildArgs(String stringArgs) { 
        return buildArgs(null, null, stringArgs);
    }
    
    public String[] buildArgs(Player player, DenizenNPC npc, String stringArgs) {
        return buildArgs(player, npc, stringArgs, true);
    }
    
    public String[] buildArgs(Player player, DenizenNPC npc, String stringArgs, boolean verbose) {
        if (stringArgs == null) return null;
        List<String> matchList = new ArrayList<String>();
        Matcher regexMatcher = regex.matcher(stringArgs);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) 
                matchList.add(plugin.tagManager().tag(player, npc, regexMatcher.group(1), true));
            else if (regexMatcher.group(2) != null) 
                matchList.add(plugin.tagManager().tag(player, npc, regexMatcher.group(2), true));
            else
                matchList.add(plugin.tagManager().tag(player, npc, regexMatcher.group(), true));
        }
        if (verbose) dB.echoDebug(ChatColor.GRAY + "Args: " + Arrays.toString(matchList.toArray()));

        String[] split = new String[matchList.size()];
        matchList.toArray(split);
        return split;
    }

    /* 
     * Builds ScriptEntry(ies) of items read from a script 
     */

    public List<ScriptEntry> buildScriptEntries(DenizenNPC npc, List<String> script, String scriptName) {
        return buildScriptEntries(null, npc, script, scriptName, null, null, null);
    }

    public List<ScriptEntry> buildScriptEntries(Player player, List<String> script, String scriptName) {
        return buildScriptEntries(player, null, script, scriptName, null, null, null);
    }

    public List<ScriptEntry> buildScriptEntries(Player player, DenizenNPC npc, List<String> script, String scriptName, String step) {
        return buildScriptEntries(player, npc, script, scriptName, step, null, null);
    }

    public List<ScriptEntry> buildScriptEntries(Player player, DenizenNPC npc, List<String> script, String scriptName, String step, String playerText, String formattedText) {
        List<ScriptEntry> scriptCommands = new ArrayList<ScriptEntry>();

        if (script == null || script.isEmpty()) {
            dB.echoError("Building script entries... no entries to build!");
            return null;
        }

        dB.echoDebug("Building script entries:");

        for (String thisItem : script) {
            // ENGAGE NOW functionality engages the NPC at the soonest possible point.
            // TODO: Possibly enable NOW argument on ALL commands?
            if (thisItem.toUpperCase().contains("ENGAGE")
                    && thisItem.toUpperCase().contains("NOW")) {
                plugin.getCommandRegistry().get(EngageCommand.class).setEngaged(npc.getCitizen(), true);
            }

            String[] scriptEntry = new String[2];
            if (thisItem.split(" ", 2).length == 1) {
                scriptEntry[0] = thisItem;
                scriptEntry[1] = null;
            } else {
                scriptEntry = thisItem.split(" ", 2);
            }

            try {
                /* Build new script commands */
                String[] args = buildArgs(player, npc, scriptEntry[1], false);
                dB.echoDebug("Adding '" + scriptEntry[0] + "'  Args: " + Arrays.toString(args));
                scriptCommands.add(new ScriptEntry(scriptEntry[0], args, player, npc, scriptName, step, playerText, formattedText));
            } catch (Exception e) {
                if (dB.showStackTraces) e.printStackTrace();
            }
        }

        return scriptCommands;
    }

    /*
     * Methods for adding Script Entries to the queue
     */

    public void queueScriptEntries(Player player, List<ScriptEntry> scriptEntries, QueueType queueType) {
        if (scriptEntries == null || scriptEntries.isEmpty()) {
            dB.echoError("Queueing up script... no entries to queue!");
            dB.echoDebug(DebugElement.Footer);
            return;
        }

        Map<Player, List<ScriptEntry>> thisQueue = plugin.getScriptEngine().getQueue(queueType);
        List<ScriptEntry> existingScriptEntries = new ArrayList<ScriptEntry>();

        if (thisQueue.containsKey(player)) {
            existingScriptEntries.addAll(thisQueue.get(player));
            thisQueue.remove(player); 
        }

        existingScriptEntries.addAll(scriptEntries);

        thisQueue.put(player, existingScriptEntries);
        dB.echoApproval("Queueing up script... entries added!");
        dB.echoDebug(DebugElement.Footer);
    }

    // For Denizen Activity Queue
    public void queueScriptEntries(DenizenNPC npc, List<ScriptEntry> scriptEntries, QueueType queueType) {
        if (scriptEntries == null || scriptEntries.isEmpty()) {
            dB.echoError("Queueing up script... no entries to queue!");
            dB.echoDebug(DebugElement.Footer);
            return;
        }

        Map<DenizenNPC, List<ScriptEntry>> thisQueue = plugin.getScriptEngine().getDQueue(queueType);
        List<ScriptEntry> existingScriptEntries = new ArrayList<ScriptEntry>();

        if (thisQueue.containsKey(npc)) {
            existingScriptEntries.addAll(thisQueue.get(npc));
            thisQueue.remove(npc); 
        }

        existingScriptEntries.addAll(scriptEntries);

        thisQueue.put(npc, existingScriptEntries);
        dB.echoApproval("Queueing up script... entries added!");
        dB.echoDebug(DebugElement.Footer);
    }

    
    /**
     * Cheater method for running a Player task script. Will automatically
     * build script entries, arguments, and queue to a Player_Task Queue.
     * 
     * @param player
     * 		The player whose queue to use.
     * @param scriptName
     * 		The name of the task script.
     */
    public void runTaskScript(Player player, String scriptName) {
        List<String> theScript = plugin.getScriptEngine().getScriptHelper().getScriptContents(scriptName + ".SCRIPT");
        // Build scriptEntries from the script and queue them up
        queueScriptEntries(player, buildScriptEntries(player, theScript, scriptName), QueueType.PLAYER_TASK);
    }
    
    /**
     * Cheater method for running a Player task script with a NPC attached. Will automatically
     * build script entries, arguments, and queue to a Player_Task Queue.
     * 
     * @param player
     * 		The player whose queue to use.
     * @param npc
     * 		The DenizenNPC object of which to attach to the scriptEntries.
     * @param scriptName
     * 		The name of the task script.
     */
    public void runTaskScript(Player player, DenizenNPC npc, String scriptName) {
        List<String> theScript = plugin.getScriptEngine().getScriptHelper().getScriptContents(scriptName + ".SCRIPT");
        // Build scriptEntries from the script and queue them up
        queueScriptEntries(player, buildScriptEntries(player, npc, theScript, scriptName, null), QueueType.PLAYER_TASK);
    }

    /**
     * Cheater method for running a NPC task script. Will automatically
     * build script entries, arguments, and queue to a NPC Queue.
     * 
     * @param player
     * 		The player whose queue to use.
     * @param npc
     * 		The DenizenNPC object of which to attach to the scriptEntries.
     * @param scriptName
     * 		The name of the task script.
     */
    public void runTaskScript(DenizenNPC npc, String scriptName) {
        List<String> theScript = plugin.getScriptEngine().getScriptHelper().getScriptContents(scriptName + ".SCRIPT");
        // Build scriptEntries from the script and queue them up
        queueScriptEntries(npc, buildScriptEntries(npc, theScript, scriptName), QueueType.NPC);
    }
    
    /**
     * Cheater method for running a NPC task script with a Player attached. Will automatically
     * build script entries, arguments, and queue to a NPC Queue.
     * 
     * @param player
     * 		The player whose queue to use.
     * @param npc
     * 		The DenizenNPC object of which to attach to the scriptEntries.
     * @param scriptName
     * 		The name of the task script.
     */
    public void runTaskScript(DenizenNPC npc, Player player, String scriptName) {
        List<String> theScript = plugin.getScriptEngine().getScriptHelper().getScriptContents(scriptName + ".SCRIPT");
        // Build scriptEntries from the script and queue them up
        queueScriptEntries(npc, buildScriptEntries(player, npc, theScript, scriptName, null), QueueType.NPC);
    }
    
}
