package de.ks.standbein.table;

import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.i18n.LocalizationModule;
import de.ks.util.FXPlatform;
import javafx.scene.control.TableView;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;

public class TableConfiguratorTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule()).launchServices();

  @Inject
  TableConfigurator<MyTableItem> configurator;
  @Inject
  @Named(LocalizationModule.DATETIME_FORMAT)
  DateTimeFormatter dateTimeFormatter;
  @Inject
  @Named(LocalizationModule.DATE_FORMAT)
  DateTimeFormatter dateFormatter;

  @Test
  public void testConfigureTable() throws Exception {
    TableView<MyTableItem> tableView = new TableView<>();
    configurator.addText(MyTableItem.class, MyTableItem::getName);
    configurator.addDateTime(MyTableItem.class, MyTableItem::getTime);
    configurator.addNumber(MyTableItem.class, MyTableItem::getCount);
    configurator.addNumber(MyTableItem.class, MyTableItem::getSum);
    configurator.addDate(MyTableItem.class, MyTableItem::getDate);
    configurator.configureTable(tableView);

    assertEquals(5, tableView.getColumns().size());

    LocalDateTime dateTime = LocalDateTime.of(2015, 12, 24, 15, 10);
    LocalDate date = LocalDate.of(1968, 7, 7);
    MyTableItem item = new MyTableItem().setName("Hello").setCount((short) 12).setSum(42.42).setTime(dateTime).setDate(date);
    FXPlatform.invokeLater(() -> tableView.getItems().add(item));


    assertEquals("Hello", tableView.getColumns().get(0).getCellObservableValue(item).getValue());
    assertEquals(dateTimeFormatter.format(dateTime), tableView.getColumns().get(1).getCellObservableValue(item).getValue());
    assertEquals(12, tableView.getColumns().get(2).getCellObservableValue(item).getValue());
    assertEquals(42.42, tableView.getColumns().get(3).getCellObservableValue(item).getValue());
    assertEquals(dateFormatter.format(date), tableView.getColumns().get(4).getCellObservableValue(item).getValue());
  }
}