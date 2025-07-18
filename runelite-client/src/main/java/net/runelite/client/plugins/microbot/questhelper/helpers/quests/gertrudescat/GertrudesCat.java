/*
 * Copyright (c) 2020, Patyfatycake <https://github.com/Patyfatycake/>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABI`LITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.gertrudescat;

import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirements;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicType;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ExperienceReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.ItemReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.UnlockReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

import java.util.*;

public class GertrudesCat extends BasicQuestHelper
{
	//Items Required
	ItemRequirement bucketOfMilk, coins, seasonedSardine, sardine, doogleLeaves, milkHighlighted,
		seasonedSardineHighlighted, kittenHighlighted;

	ItemRequirement lumberyardTeleport, varrockTeleport;

	QuestStep talkToGertrude, talkToChildren, gertrudesCat, gertrudesCat2, searchNearbyCrates,
		giveKittenToFluffy, finishQuest;

	QuestStep pickupDoogle, makeSeasonedSardine;

	ConditionalStep giveMilkToCatSteps, giveSardineToCat;

	Requirement isUpstairsLumberyard, hasFluffsKitten;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();

		return getSteps();
	}

	private Map<Integer, QuestStep> getSteps()
	{
		Map<Integer, QuestStep> steps = new HashMap<>();

		steps.put(0, talkToGertrude = getTalkToGertrude());

		talkToChildren = getTalkToChildren();

		ConditionalStep conditionalTalkToChildren = new ConditionalStep(this, pickupDoogle);
		conditionalTalkToChildren.addStep(new ItemRequirements(LogicType.AND, "", sardine, doogleLeaves), makeSeasonedSardine);
		conditionalTalkToChildren.addStep(seasonedSardine, talkToChildren);
		steps.put(1, conditionalTalkToChildren);

		steps.put(2, giveMilkToCatSteps = getGiveMilkToCat());
		steps.put(3, giveSardineToCat = getFeedCat());
		steps.put(4, findFluffsKitten());
		steps.put(5, finishQuest = returnToGertrude());
		return steps;
	}

	private NpcStep returnToGertrude()
	{
		return new NpcStep(this, NpcID.GERTRUDE_QUEST,
			new WorldPoint(3148, 3413, 0), "Return to Gertrude to complete the quest.");
	}

	private QuestStep findFluffsKitten()
	{
		//Need to find to ways to hide arrow
		searchNearbyCrates = new NpcStep(this, NpcID.KITTENS_MEW, new WorldPoint(3306, 3505, 0),
			"Search for a kitten in the crates in the Lumberyard.", true);
		((NpcStep)(searchNearbyCrates)).setHideWorldArrow(true);
		ObjectStep climbDownLadderStep = goDownLadderStep();
		ObjectStep climbUpLadderStep = getClimbLadder();
		ArrayList<ItemRequirement> fluffsKittenRequirement = new ArrayList<>();
		fluffsKittenRequirement.add(new ItemRequirement("Fluffs' Kitten", ItemID.GERTRUDEKITTENS));
		climbUpLadderStep.addItemRequirements(fluffsKittenRequirement);
		Conditions hasFluffsKittenUpstairs = new Conditions(hasFluffsKitten, isUpstairsLumberyard);

		kittenHighlighted = new ItemRequirement("Fluffs' Kitten", ItemID.GERTRUDEKITTENS);
		kittenHighlighted.setHighlightInInventory(true);

		giveKittenToFluffy = getGertrudesCat(kittenHighlighted);
		giveKittenToFluffy.setText("Return the kitten to Gertrude's cat.");
		giveKittenToFluffy.addIcon(ItemID.GERTRUDEKITTENS);

		ConditionalStep conditionalKitten = new ConditionalStep(this, searchNearbyCrates);
		conditionalKitten.addStep(hasFluffsKittenUpstairs, giveKittenToFluffy);
		conditionalKitten.addStep(hasFluffsKitten, climbUpLadderStep);
		conditionalKitten.addStep(isUpstairsLumberyard, climbDownLadderStep);

		searchNearbyCrates.addSubSteps(climbDownLadderStep);
		giveKittenToFluffy.addSubSteps(climbUpLadderStep);

		return conditionalKitten;
	}

	private ObjectStep goDownLadderStep()
	{
		return new ObjectStep(this, ObjectID.FAI_VARROCK_LADDERTOP, new WorldPoint(3310, 3509, 1),
			"Climb down ladder in the Lumberyard.");
	}

	private ConditionalStep getFeedCat()
	{
		gertrudesCat2 = getGertrudesCat(seasonedSardineHighlighted);
		gertrudesCat2.addIcon(ItemID.SEASONED_SARDINE);

		ObjectStep climbLadder = new ObjectStep(this, ObjectID.FAI_VARROCK_LADDER,
			new WorldPoint(3310, 3509, 0), "Climb up the ladder in the Lumberyard.", seasonedSardine);

		ConditionalStep lumberyard = new ConditionalStep(this, climbLadder, "Use a seasoned sardine on Gertrude's cat upstairs in the Lumberyard north east of Varrock.");
		lumberyard.addStep(isUpstairsLumberyard, gertrudesCat2);
		gertrudesCat2.addSubSteps(climbLadder);

		return lumberyard;
	}

	private ConditionalStep getGiveMilkToCat()
	{
		gertrudesCat = getGertrudesCat(milkHighlighted);
		gertrudesCat.addIcon(ItemID.BUCKET_MILK);

		ObjectStep climbLadder = getClimbLadder(bucketOfMilk);

		ConditionalStep giveMilkToCat = new ConditionalStep(this, climbLadder, "Use a bucket of milk on Gertrude's cat upstairs in the Lumberyard north east of Varrock.", seasonedSardine);
		giveMilkToCat.addStep(isUpstairsLumberyard, gertrudesCat);

		return giveMilkToCat;
	}

	private NpcStep getGertrudesCat(ItemRequirement... requirement)
	{
		return new NpcStep(this, NpcID.GERTRUDESCAT,
			new WorldPoint(3308, 3511, 1), "", requirement);
	}

	private QuestStep getTalkToChildren()
	{
		pickupDoogle = new DetailedQuestStep(this, "Pickup some Doogle Leaves south of Gertrude's house.", new ItemRequirement("Doogle Leaves", ItemID.DOOGLELEAVES), sardine);
		makeSeasonedSardine = new DetailedQuestStep(this, "Use your Doogle Leaves on  the Sardine.", sardine, doogleLeaves);

		NpcStep talkToChildren = new NpcStep(this, NpcID.SHILOP,
			new WorldPoint(3222, 3435, 0), "Talk to Shilop or Wilough in the Varrock Square.", true,
			seasonedSardine, coins);
		talkToChildren.addAlternateNpcs(NpcID.WILOUGH);
		talkToChildren.addDialogSteps("What will make you tell me?", "Okay then, I'll pay.");

		return talkToChildren;
	}

	private QuestStep getTalkToGertrude()
	{
		NpcStep talkToGertrude = new NpcStep(this, NpcID.GERTRUDE_QUEST,
			new WorldPoint(3148, 3413, 0), "Talk to Gertrude.");
		talkToGertrude.addDialogStep("Yes.");
		return talkToGertrude;
	}

	@Override
	protected void setupRequirements()
	{
		bucketOfMilk = new ItemRequirement("Bucket of milk", ItemID.BUCKET_MILK);
		milkHighlighted = new ItemRequirement("Bucket of milk", ItemID.BUCKET_MILK);
		milkHighlighted.setHighlightInInventory(true);

		coins = new ItemRequirement("Coins", ItemCollections.COINS, 100);

		seasonedSardine = new ItemRequirement("Seasoned Sardine", ItemID.SEASONED_SARDINE);
		seasonedSardine.setTooltip("Can be created by using a sardine on Doogle leaves(South of Gertrudes House)");

		seasonedSardineHighlighted = new ItemRequirement("Seasoned Sardine", ItemID.SEASONED_SARDINE);
		seasonedSardineHighlighted.setTooltip("Can be created by using a sardine on Doogle leaves(South of Gertrudes House)");
		seasonedSardineHighlighted.setHighlightInInventory(true);

		sardine = new ItemRequirement("Raw Sardine", ItemID.RAW_SARDINE);
		sardine.setHighlightInInventory(true);
		doogleLeaves = new ItemRequirement("Doogle Leaves", ItemID.DOOGLELEAVES);
		doogleLeaves.setHighlightInInventory(true);

		// Recommended items
		lumberyardTeleport = new ItemRequirement("Lumberyard teleport", ItemID.TELEPORTSCROLL_LUMBERYARD);
		varrockTeleport = new ItemRequirement("Varrock teleports", ItemID.POH_TABLET_VARROCKTELEPORT, 2);
	}

	@Override
	protected void setupZones()
	{
		Zone zone = new Zone(new WorldPoint(3306, 3507, 12), new WorldPoint(3312, 3513, 1));

		isUpstairsLumberyard = new ZoneRequirement(zone);
	}

	private void setupConditions()
	{
		hasFluffsKitten = new ItemRequirements(new ItemRequirement("Fluffs' kitten", ItemID.GERTRUDEKITTENS));
	}

	private ObjectStep getClimbLadder(ItemRequirement... itemRequirements)
	{
		return new ObjectStep(this, ObjectID.FAI_VARROCK_LADDER,
			new WorldPoint(3310, 3509, 0), "Climb up the ladder in the Lumberyard.", itemRequirements);
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		return Arrays.asList(bucketOfMilk, coins, sardine);
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Arrays.asList(varrockTeleport, lumberyardTeleport);
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(1);
	}

	@Override
	public List<ExperienceReward> getExperienceRewards()
	{
		return Collections.singletonList(new ExperienceReward(Skill.COOKING, 1525));
	}

	@Override
	public List<ItemReward> getItemRewards()
	{
		return Arrays.asList(
				new ItemReward("A pet Kitten", ItemID.KITTENOBJECT, 1),
				new ItemReward("Chocolate Cake", ItemID.CHOCOLATE_CAKE, 1),
				new ItemReward("Stew", ItemID.STEW, 1));
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Collections.singletonList(new UnlockReward("Ability to raise kittens."));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> steps = new ArrayList<>();

		PanelDetails startingPanel = new PanelDetails("Starting out",
			Arrays.asList(talkToGertrude, pickupDoogle, makeSeasonedSardine, talkToChildren),
			sardine, coins);
		steps.add(startingPanel);

		PanelDetails lumberYardPanel = new PanelDetails("The secret playground (Lumber Yard)",
			Arrays.asList(giveMilkToCatSteps, giveSardineToCat, searchNearbyCrates, giveKittenToFluffy),
			seasonedSardine, bucketOfMilk);
		steps.add(lumberYardPanel);

		PanelDetails finishQuestPanel = new PanelDetails("Finish the quest",
			Collections.singletonList(finishQuest));
		steps.add(finishQuestPanel);
		return steps;
	}
}
