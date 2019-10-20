/*
 * This file is part of Total Economy, licensed under the MIT License (MIT).
 *
 * Copyright (c) Eric Grandt <https://www.ericgrandt.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.erigitic.main;

import com.erigitic.commands.*;
import com.erigitic.config.AccountManager;
import com.erigitic.config.TECurrency;
import com.erigitic.config.TECurrencyRegistryModule;
import com.erigitic.jobs.JobManager;
import com.erigitic.shops.Shop;
import com.erigitic.shops.ImmutableShopData;
import com.erigitic.shops.data.*;
import com.erigitic.shops.ShopData;
import com.erigitic.shops.ShopManager;
import com.erigitic.sql.SqlManager;
import com.erigitic.util.MessageManager;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

@Plugin(id = "totaleconomy", name = "Total Economy", version = "1.8.2", description = "All in one economy plugin for Minecraft/Sponge")
public class TotalEconomy {

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private File defaultConf;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    @Inject
    private Game game;

    @Inject
    private PluginContainer pluginContainer;

    private UserStorageService userStorageService;

    private ConfigurationNode config;

    private static TotalEconomy totalEconomy;
    private TECurrency defaultCurrency;
    private SqlManager sqlManager;
    private AccountManager accountManager;
    private JobManager jobManager;
    private MessageManager messageManager;
    private ShopManager shopManager;

    private TECurrencyRegistryModule teCurrencyRegistryModule;

    private HashSet<Currency> currencies = new HashSet<>();

    private String languageTag;

    private int saveInterval;

    // Job Variables
    private boolean jobFeatureEnabled = true;
    private boolean jobNotificationEnabled = true;
    private boolean jobSalaryEnabled = true;

    // Shop Variables
    private boolean chestShopEnabled = true;

    // Database Variables
    private boolean databaseEnabled = false;
    private String databaseUrl;
    private String databaseUser;
    private String databasePassword;

    // Money Cap Variables
    private boolean moneyCapEnabled = false;
    private BigDecimal moneyCap;

    private DataRegistration<ShopData, ImmutableShopData> shopDataRegistration;

    @Listener
    public void preInit(GamePreInitializationEvent event) {
        totalEconomy = this;

        loadConfig();
        loadCurrencies();
        setFeaturesEnabledStatus();

        languageTag = config.getNode("language").getString("en");
        saveInterval = config.getNode("save-interval").getInt(30);

        if (databaseEnabled) {
            databaseUrl = config.getNode("database", "url").getString();
            databaseUser = config.getNode("database", "user").getString();
            databasePassword = config.getNode("database", "password").getString();

            sqlManager = new SqlManager(this, logger);
        }

        messageManager = new MessageManager(this, logger, Locale.forLanguageTag(languageTag));
        accountManager = new AccountManager(this, messageManager, logger);
        teCurrencyRegistryModule = new TECurrencyRegistryModule(this);

        game.getServiceManager().setProvider(this, EconomyService.class, accountManager);

        // Only create JobManager
        if (jobFeatureEnabled) {
            jobManager = new JobManager(this, accountManager, messageManager, logger);
        }

        if (moneyCapEnabled) {
            moneyCap = BigDecimal.valueOf(config.getNode("features", "moneycap", "amount").getFloat()).setScale(2, BigDecimal.ROUND_DOWN);
        }

        if (chestShopEnabled) {
            shopManager = new ShopManager();
//            shopManager = new ShopManager(this, accountManager, messageManager);
        }

        // Allows for retrieving of all/individual currencies in Total Economy by other plugins
        game.getRegistry().registerModule(Currency.class, teCurrencyRegistryModule);
    }

    @Listener
    public void init(GameInitializationEvent event) {
        registerCommands();
        registerListeners();
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        userStorageService = game.getServiceManager().provideUnchecked(UserStorageService.class);

        logger.info("Total Economy Started");
    }

    @Listener(order = Order.LAST)
    public void onServerStopping(GameStoppingServerEvent event) {
        logger.info("Total Economy Stopping");

        if (!databaseEnabled) {
            accountManager.saveConfiguration();
        }
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        logger.info("Total Economy Stopped");
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();

        accountManager.getOrCreateAccount(player.getUniqueId());
    }

    /**
     * Reloads configuration files.
     *
     * @param event GameReloadEvent
     */
    @Listener
    public void onGameReload(GameReloadEvent event) {
        if (jobFeatureEnabled) {
            jobManager.reloadJobsAndSets();
        }

        accountManager.reloadConfig();
    }

    @Listener
    public void onKeyRegistration(GameRegistryEvent.Register<Key<?>> event) {
        event.register(ShopKeys.SHOP);
    }

    @Listener
    public void onDataRegistration(GameRegistryEvent.Register<DataRegistration<?, ?>> event) {
        final DataManager dataManager = Sponge.getDataManager();

        dataManager.registerBuilder(Shop.class, new ShopBuilder());

        shopDataRegistration = DataRegistration.builder()
                .dataClass(ShopData.class)
                .immutableClass(ImmutableShopData.class)
                .dataImplementation(ShopDataImpl.class)
                .immutableImplementation(ImmutableShopDataImpl.class)
                .builder(new ShopDataBuilder())
                .dataName("Shop Data")
                .manipulatorId("shop")
                .buildAndRegister(pluginContainer);
    }

    /**
     * Load the default config file, totaleconomy.conf.
     */
    private void loadConfig() {
        try {
            if (!defaultConf.exists()) {
                pluginContainer.getAsset("totaleconomy.conf").get().copyToFile(defaultConf.toPath());
            }

            config = loader.load();
        } catch (IOException e) {
            logger.warn("[TE] Main configuration file could not be loaded/created/changed!", e);
        }
    }

    /**
     * Create commands and registers them with the CommandManager.
     */
    private void registerCommands() {
        game.getCommandManager().register(this, AdminPayCommand.commandSpec(), "adminpay");
        game.getCommandManager().register(this, BalanceCommand.commandSpec(), "balance", "bal", "money");
        game.getCommandManager().register(this, BalanceTopCommand.commandSpec(), "balancetop", "baltop");
        game.getCommandManager().register(this, PayCommand.commandSpec(), "pay");
        game.getCommandManager().register(this, SetBalanceCommand.commandSpec(), "setbalance", "setbal");
        game.getCommandManager().register(this, ViewBalanceCommand.commandSpec(), "viewbalance", "vbal");

        if (jobFeatureEnabled) {
            game.getCommandManager().register(this, new JobCommand().commandSpec(), "job");
        }

        if (chestShopEnabled) {
            game.getCommandManager().register(this, new ShopCommand().commandSpec(), "shop");
        }
    }

    /**
     * Load each currency from the default configuration file. Sets the default currency.
     */
    private void loadCurrencies() {
        config.getNode("currency").getChildrenMap().keySet().forEach(currencyName -> {
            ConfigurationNode currencyNode = config.getNode("currency", currencyName.toString());

            String currencySingular = currencyNode.getNode("currency-singular").getString();
            String currencyPlural = currencyNode.getNode("currency-plural").getString();
            String currencySymbol = currencyNode.getNode("symbol").getString();
            boolean isDefault = currencyNode.getNode("default").getBoolean();
            boolean prefixSymbol = currencyNode.getNode("prefix-symbol").getBoolean();
            boolean isTransferable = currencyNode.getNode("transferable").getBoolean();
            BigDecimal startBalance = new BigDecimal(currencyNode.getNode("startbalance").getDouble());

            TECurrency currency = new TECurrency(
                    Text.of(currencySingular),
                    Text.of(currencyPlural),
                    Text.of(currencySymbol),
                    2,
                    isDefault,
                    prefixSymbol,
                    isTransferable,
                    startBalance
            );

            if (isDefault) {
                defaultCurrency = currency;
            }

            currencies.add(currency);
        });
    }

    /**
     * Register event listeners.
     */
    private void registerListeners() {
        EventManager eventManager = game.getEventManager();

        if (jobFeatureEnabled) {
            eventManager.registerListeners(this, jobManager);
        }

        if (chestShopEnabled) {
            eventManager.registerListeners(this, shopManager);
        }
    }

    /**
     * Determines what features to enable from the main configuration file. Sets the corresponding features boolean to true/false (enabled/disabled).
     */
    private void setFeaturesEnabledStatus() {
        jobFeatureEnabled = config.getNode("features", "jobs", "enable").getBoolean(true);
        jobNotificationEnabled = config.getNode("features", "jobs", "notifications").getBoolean(true);
        jobSalaryEnabled = config.getNode("features", "jobs", "salary").getBoolean(true);
        databaseEnabled = config.getNode("database", "enable").getBoolean(false);
        moneyCapEnabled = config.getNode("features", "moneycap", "enable").getBoolean(true);
        chestShopEnabled = config.getNode("features", "shops", "chestshop", "enable").getBoolean(true);
    }

    public static TotalEconomy getTotalEconomy() {
        return totalEconomy;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public ConfigurationNode getShopNode() {
        return config.getNode("features", "shops");
    }

    public HashSet<Currency> getCurrencies() {
        return currencies;
    }

    public TECurrencyRegistryModule getTECurrencyRegistryModule() {
        return teCurrencyRegistryModule;
    }

    public File getConfigDir() {
        return configDir;
    }

    public Server getServer() {
        return game.getServer();
    }

    public Game getGame() {
        return game;
    }

    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }

    public TECurrency getDefaultCurrency() {
        return defaultCurrency;
    }

    public boolean isJobSalaryEnabled() {
        return jobSalaryEnabled;
    }

    public boolean isDatabaseEnabled() {
        return databaseEnabled;
    }

    public boolean isJobNotificationEnabled() {
        return jobNotificationEnabled;
    }

    public int getSaveInterval() {
        return saveInterval;
    }

    public BigDecimal getMoneyCap() {
        return moneyCapEnabled ? moneyCap : new BigDecimal(Double.MAX_VALUE);
    }

    public UserStorageService getUserStorageService() {
        return userStorageService;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public SqlManager getSqlManager() {
        return sqlManager;
    }

    public Logger getLogger() {
        return logger;
    }
}