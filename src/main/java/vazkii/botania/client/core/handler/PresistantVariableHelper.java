/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jun 24, 2015, 5:54:45 PM (GMT)]
 */
package vazkii.botania.client.core.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import vazkii.botania.client.challenge.Challenge;
import vazkii.botania.client.challenge.ModChallenges;
import vazkii.botania.client.gui.lexicon.GuiLexicon;
import vazkii.botania.client.gui.lexicon.GuiLexiconEntry;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibMisc;

public final class PresistantVariableHelper {

	private static final String TAG_FIRST_LOAD = "firstLoad";
	private static final String TAG_BOOKMARK_COUNT = "bookmarkCount";
	private static final String TAG_BOOKMARK_PREFIX = "bookmark";
	private static final String TAG_BOOKMARKS = "bookmarks";
	private static final String TAG_CHALLENGES = "challenges";
	private static final String TAG_LEXICON_NOTES = "lexiconNotes";
	private static final String TAG_LAST_BOTANIA_VERSION = "lastBotaniaVersion";

	private static File cacheFile;

	public static boolean firstLoad = true;
	public static String lastBotaniaVersion = "";

	public static void save() throws IOException {
		NBTTagCompound cmp = new NBTTagCompound();

		List<GuiLexicon> bookmarks = GuiLexicon.bookmarks;
		int count = bookmarks.size();
		cmp.setInteger(TAG_BOOKMARK_COUNT, count);
		NBTTagCompound bookmarksCmp = new NBTTagCompound();
		for(int i = 0; i < count; i++) {
			GuiLexicon lex = bookmarks.get(i);
			NBTTagCompound bookmarkCmp = new NBTTagCompound();
			lex.serialize(bookmarkCmp);
			bookmarksCmp.setTag(TAG_BOOKMARK_PREFIX + i, bookmarkCmp);
		}
		cmp.setTag(TAG_BOOKMARKS, bookmarksCmp);

		NBTTagCompound challengesCmp = new NBTTagCompound();
		for(Challenge c : ModChallenges.challengeLookup.values())
			c.writeToNBT(challengesCmp);
		cmp.setTag(TAG_CHALLENGES, challengesCmp);

		NBTTagCompound notesCmp = new NBTTagCompound();
		for(String s : GuiLexicon.notes.keySet()) {
			String note = GuiLexicon.notes.get(s);
			if(note != null && !note.trim().isEmpty())
				notesCmp.setString(s, note);
		}
		cmp.setTag(TAG_LEXICON_NOTES, notesCmp);

		cmp.setBoolean(TAG_FIRST_LOAD, firstLoad);
		cmp.setString(TAG_LAST_BOTANIA_VERSION, lastBotaniaVersion);

		injectNBTToFile(cmp, getCacheFile());
	}

	public static void load() throws IOException {
		NBTTagCompound cmp = getCacheCompound();

		int count = cmp.getInteger(TAG_BOOKMARK_COUNT);
		GuiLexicon.bookmarks.clear();
		if(count > 0) {
			NBTTagCompound bookmarksCmp = cmp.getCompoundTag(TAG_BOOKMARKS);
			for(int i = 0; i < count; i++) {
				NBTTagCompound bookmarkCmp = bookmarksCmp.getCompoundTag(TAG_BOOKMARK_PREFIX + i);
				GuiLexicon gui = GuiLexicon.create(bookmarkCmp);
				if(gui != null)
					GuiLexicon.bookmarks.add(gui);
			}
		}

		if(cmp.hasKey(TAG_CHALLENGES)) {
			NBTTagCompound challengesCmp = cmp.getCompoundTag(TAG_CHALLENGES);
			for(Challenge c : ModChallenges.challengeLookup.values())
				c.readFromNBT(challengesCmp);
		}

		if(cmp.hasKey(TAG_LEXICON_NOTES)) {
			NBTTagCompound notesCmp = cmp.getCompoundTag(TAG_LEXICON_NOTES);
			Set<String> keys = notesCmp.func_150296_c();
			GuiLexicon.notes.clear();
			for(String key : keys)
				GuiLexicon.notes.put(key, notesCmp.getString(key));
		}

		lastBotaniaVersion = cmp.hasKey(TAG_LAST_BOTANIA_VERSION) ? cmp.getString(TAG_LAST_BOTANIA_VERSION) : "(N/A)";

		firstLoad = cmp.hasKey(TAG_FIRST_LOAD) ? cmp.getBoolean(TAG_FIRST_LOAD) : firstLoad;
		if(firstLoad) {
			GuiLexicon.currentOpenLexicon = new GuiLexiconEntry(LexiconData.welcome, new GuiLexicon());
			lastBotaniaVersion = LibMisc.VERSION;
		}
	}

	public static void saveSafe() {
		try {
			save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setCacheFile(File f) {
		cacheFile = f;
	}

	public static File getCacheFile() throws IOException {
		if(!cacheFile.exists())
			cacheFile.createNewFile();

		return cacheFile;
	}

	public static NBTTagCompound getCacheCompound() throws IOException {
		return getCacheCompound(getCacheFile());
	}

	public static NBTTagCompound getCacheCompound(File cache) throws IOException {
		if(cache == null)
			throw new RuntimeException("No cache file!");

		try {
			NBTTagCompound cmp = CompressedStreamTools.readCompressed(new FileInputStream(cache));
			return cmp;
		} catch(IOException e) {
			NBTTagCompound cmp = new NBTTagCompound();
			CompressedStreamTools.writeCompressed(cmp, new FileOutputStream(cache));
			return getCacheCompound(cache);
		}
	}

	public static void injectNBTToFile(NBTTagCompound cmp, File f) {
		try {
			CompressedStreamTools.writeCompressed(cmp, new FileOutputStream(f));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

}
