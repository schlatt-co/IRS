package io.github.jroy.irs;

import dev.tycho.stonks.managers.Repo;
import dev.tycho.stonks.model.core.Account;
import dev.tycho.stonks.model.core.Company;
import net.ess3.api.events.UserBalanceUpdateEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.math.BigDecimal;
import java.math.MathContext;

public class EconomyManager implements Listener {

  private static final BigDecimal incomingTaxRate = new BigDecimal(0.06);
  private static final BigDecimal outgoingTaxRate = new BigDecimal(0.04);

  @EventHandler
  public void onBalanceUpdate(UserBalanceUpdateEvent event) {
    event.setNewBalance(processTransaction(event));
  }

  private BigDecimal processTransaction(UserBalanceUpdateEvent event) {
    if (event.getPlayer().hasPermission("irs.taxexempt") || event.getNewBalance().intValue() < event.getOldBalance().intValue()) { //Not an incoming payment
      return event.getNewBalance();
    }

    BigDecimal difference = event.getNewBalance().subtract(event.getOldBalance());
    if (difference.intValue() < 10000) { //Don't tax transactions under 10k
      return event.getNewBalance();
    }

    if (event.getOldBalance().intValue() > 1000000) { //Receiving party has over a 1M
      if (event.getPlayer().isOnline()) {
        event.getPlayer().sendMessage(ChatColor.AQUA + "IRS>> " + ChatColor.YELLOW + "You've been taxed 10% of your incoming $" + difference.intValue() + " ($" + difference.multiply(incomingTaxRate).intValue() + ")");
      }
      depositTaxAccount(event.getPlayer(), difference.multiply(incomingTaxRate).intValue());
      return event.getNewBalance().subtract(difference.multiply(incomingTaxRate).round(new MathContext(1)));
    } else if (difference.intValue() > 100000) { //Receiving party is getting over 100k
      if (event.getPlayer().isOnline()) {
        event.getPlayer().sendMessage(ChatColor.AQUA + "IRS>> " + ChatColor.YELLOW + "You've been taxed 7% of your incoming $" + difference.intValue() + " ($" + difference.multiply(outgoingTaxRate).intValue() + ")");
      }
      depositTaxAccount(event.getPlayer(), difference.multiply(outgoingTaxRate).intValue());
      return event.getNewBalance().subtract(difference.multiply(outgoingTaxRate).round(new MathContext(1)));
    } else { //Tax-free transaction
      return event.getNewBalance();
    }
  }

  private void depositTaxAccount(Player player, double amount) {
    //Find a company for the admins
    Company c = Repo.getInstance().companies().getWhere(co->co.name.equals("Admins"));
    if (c == null) return;
    Account account = c.accounts.stream().filter(a -> a.name.equals("Taxes")).findFirst().orElse(null);
    if (account == null) {
      account = Repo.getInstance().createCompanyAccount(c, "Taxes");
    }
    Repo.getInstance().payAccount(player.getUniqueId(), "Taxes payment", account, amount);
  }
}
