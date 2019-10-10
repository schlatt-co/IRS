package io.github.jroy.irs;

import dev.tycho.stonks.managers.DatabaseHelper;
import dev.tycho.stonks.model.core.Account;
import dev.tycho.stonks.model.core.AccountLink;
import net.ess3.api.events.UserBalanceUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;

public class EconomyManager implements Listener {

  private static final BigDecimal incomingTaxRate = new BigDecimal(0.10);
  private static final BigDecimal outgoingTaxRate = new BigDecimal(0.07);

  @EventHandler
  public void onBalanceUpdate(UserBalanceUpdateEvent event) {
    event.setNewBalance(processTransaction(event));
  }

  private BigDecimal processTransaction(UserBalanceUpdateEvent event) {
    if (event.getPlayer().hasPermission("trevor.taxexempt") || event.getNewBalance().intValue() < event.getOldBalance().intValue()) { //Not an incoming payment
      return event.getNewBalance();
    }

    BigDecimal difference = event.getNewBalance().subtract(event.getOldBalance());
    if (event.getOldBalance().intValue() > 1000000) { //Receiving party has over a 1M
      if (event.getPlayer().isOnline()) {
        event.getPlayer().sendMessage(ChatColor.AQUA + "IRS>> " + ChatColor.YELLOW + "You've been taxed 10% of your incoming $" + difference.intValue() + " ($" + difference.multiply(incomingTaxRate).intValue() + ")");
      }
      depositTaxAccount(difference.multiply(incomingTaxRate).intValue());
      return event.getNewBalance().subtract(difference.multiply(incomingTaxRate).round(new MathContext(1)));
    } else if (event.getNewBalance().intValue() > 100000) { //Receiving party is getting over 100k
      if (event.getPlayer().isOnline()) {
        event.getPlayer().sendMessage(ChatColor.AQUA + "IRS>> " + ChatColor.YELLOW + "You've been taxed 7% of your incoming $" + difference.intValue() + " ($" + difference.multiply(outgoingTaxRate).intValue() + ")");
      }
      depositTaxAccount(difference.multiply(outgoingTaxRate).intValue());
      return event.getNewBalance().subtract(difference.multiply(outgoingTaxRate).round(new MathContext(1)));
    } else { //Tax-free transaction
      return event.getNewBalance();
    }
  }

  private void depositTaxAccount(double amount) {
    Optional<AccountLink> optional = DatabaseHelper.getInstance().getCompanyByName("Admins").getAccounts().stream().filter(accountLink -> accountLink.getAccount().getName().equalsIgnoreCase("Taxes")).findAny();
    if (optional.isPresent()) {
      Account account = optional.get().getAccount();
      account.addBalance(amount);
      DatabaseHelper.getInstance().getDatabaseManager().updateAccount(account);
      return;
    }
    Bukkit.getLogger().severe("Tax account optional is not present!");
  }
}
