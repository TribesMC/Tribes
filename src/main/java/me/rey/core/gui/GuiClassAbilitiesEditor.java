package me.rey.core.gui;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.players.User;
import me.rey.core.pvp.Build;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiClassAbilitiesEditor extends GuiEditable {

    private final ArrayList<AbilityType> typesToDisplay = new ArrayList<>(Arrays.asList(
            AbilityType.SWORD,
            AbilityType.AXE,
            AbilityType.PASSIVE_A,
            AbilityType.PASSIVE_B,
            AbilityType.PASSIVE_C
    ));
    private final ClassType classType;
    private final Build toEdit;
    private final Warriors w;

    public GuiClassAbilitiesEditor(final Player player, final ClassType classType, final Build build) {
        super("Editing: &4" + build.getName(), 6, Warriors.getInstance().getPlugin());

        this.classType = classType;
        this.toEdit = build;
        this.w = Warriors.getInstance();

        final AbilityType[] secondaries = {AbilityType.BOW, AbilityType.SPADE};
        AbilityType toInclude = null;

        for (final AbilityType secondary : secondaries) {
            if (toInclude != null) {
                break;
            }

            if (Warriors.getInstance().getClassAbilities(classType).stream().map(Ability::getAbilityType).anyMatch(type -> type == secondary)) {
                toInclude = secondary;
            }
        }

		this.typesToDisplay.add(2, toInclude);

    }

    @Override
    public void setup() {

        /*
         * SETTING ICONS
         */
        for (int i = 0; i < this.typesToDisplay.size(); i++) {

            final AbilityType abilityType = this.typesToDisplay.get(i);
            if (abilityType == null) {
				continue;
			}

            final int row = 9 * (i + 1) - 9;
			this.setItem(new GuiItem(abilityType.getIcon().setName("&a&l" + abilityType.getName() + " Skills")) {
                @Override
                public void onUse(final Player player, final ClickType type, final int slot) {
                    // IGNORE
                }

            }, row);


            /*
             *  SETTING ABILITIES
             */
            int abilityCount = 0;
            for (int index = 0; index < this.w.getAbilitiesInCache().size(); index++) {

                final Ability ability = this.w.getAbilitiesInCache().get(index);

                if (ability.getClassType().equals(this.classType) && ability.getAbilityType().equals(abilityType) && abilityCount <= 7) {

                    final boolean isInBuild = this.toEdit.getAbility(ability.getAbilityType()) != null && this.toEdit.getAbility(ability.getAbilityType()).getIdLong() == ability.getIdLong();
                    final Material material = isInBuild ? Material.WRITTEN_BOOK : Material.BOOK;
                    final String name = String.format("&a&l%s &f- &a&lLevel %s/%s",
                            ability.getName(),
                            isInBuild ? this.toEdit.getAbilityLevel(ability.getAbilityType()) : 0,
                            ability.getMaxLevel());

                    final String actionOnClick = !isInBuild ? "Select" : "Upgrade to Level " + (this.toEdit.getAbilityLevel(ability.getAbilityType()) + 1);
                    final int level = !isInBuild ? 0 : this.toEdit.getAbilityLevel(ability.getAbilityType());
                    final List<String> lore = this.formatLore(Arrays.asList(ability.getDescription(level)), level, this.toEdit, ability, actionOnClick);
                    final int tokens = this.toEdit.getTokensRemaining();

					this.setItem(new GuiItem(new Item(material).setName(name).setAmount(Math.max(1, level)).setLore(lore)) {

                        @Override
                        public void onUse(final Player player, final ClickType type, final int slot) {

                            if (type == ClickType.LEFT) {
                                final Build newBuild = GuiClassAbilitiesEditor.this.toEdit;
                                if (tokens <= 0) {
									return;
								}
								GuiClassAbilitiesEditor.this.highPitch.play(player);

                                if (!isInBuild) {
                                    newBuild.setAbility(ability, 1);
                                    new User(player).editBuild(GuiClassAbilitiesEditor.this.toEdit, newBuild, GuiClassAbilitiesEditor.this.classType);
									GuiClassAbilitiesEditor.this.updateInventory();
                                }

                                if (isInBuild && (newBuild.getAbilityLevel(abilityType)) < ability.getMaxLevel()) {
                                    newBuild.setAbility(ability, newBuild.getAbilityLevel(abilityType) + 1);
                                    new User(player).editBuild(GuiClassAbilitiesEditor.this.toEdit, newBuild, GuiClassAbilitiesEditor.this.classType);
									GuiClassAbilitiesEditor.this.updateInventory();
                                }

								GuiClassAbilitiesEditor.this.updateInventory();
                            } else if (type == ClickType.RIGHT) {
                                final Build newBuild = GuiClassAbilitiesEditor.this.toEdit;

                                if (isInBuild && (newBuild.getAbilityLevel(abilityType)) > 0) {
									GuiClassAbilitiesEditor.this.lowPitch.play(player);

                                    if (newBuild.getAbilityLevel(abilityType) == 1) {
                                        newBuild.remove(newBuild.getAbility(abilityType));
                                        new User(player).editBuild(GuiClassAbilitiesEditor.this.toEdit, newBuild, GuiClassAbilitiesEditor.this.classType);
										GuiClassAbilitiesEditor.this.updateInventory();
                                        return;
                                    }

                                    newBuild.setAbility(ability, newBuild.getAbilityLevel(abilityType) - 1);
                                    new User(player).editBuild(GuiClassAbilitiesEditor.this.toEdit, newBuild, GuiClassAbilitiesEditor.this.classType);
                                }

								GuiClassAbilitiesEditor.this.updateInventory();
                            }


                        }

                    }, (row + 1) + abilityCount);

                    abilityCount++;
                }

            }

        }

        final int tokens = this.toEdit.getTokensRemaining();
        final String name = String.format("&a&l%s Skill Tokens", tokens);
		this.setItem(new GuiItem(new Item(tokens > 0 ? Material.GOLD_INGOT : Material.REDSTONE_BLOCK).setAmount(tokens > 0 ? tokens : 1).setName(name)) {

            @Override
            public void onUse(final Player player, final ClickType type, final int slot) { /** ignore*/}

        }, 8);

    }

    @Override
    public void init() {

    }

    private List<String> formatLore(final List<String> description, final int level, final Build toEdit, final Ability ability, final String actionOnClick) {
        final List<String> lore = new ArrayList<>(description);

        lore.add("");
        lore.add("");

        if (level < ability.getMaxLevel()) {
            lore.add("&eSkill Token Cost: &f" + ability.getSkillTokenCost());
            lore.add(String.format("&eSkill Tokens Remaining: &f%s/%s", toEdit.getTokensRemaining(), Build.MAX_TOKENS));
            lore.add("");
            lore.add("&aLeft-Click to " + actionOnClick);
        } else {
            lore.add("&6You have the maximum Level.");
        }
        return lore;
    }

}
