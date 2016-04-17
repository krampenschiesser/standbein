package de.ks.standbein.table.selection;

import de.ks.standbein.Condition;
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.application.Navigator;
import de.ks.standbein.table.MyTableItem;
import de.ks.standbein.table.TableConfigurator;
import de.ks.util.FXPlatform;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.objenesis.ObjenesisStd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TextFieldTableSelectionTest {
  private static final Logger log = LoggerFactory.getLogger(TextFieldTableSelectionTest.class);

  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule()).launchServices();
  @Inject
  TextFieldTableSelection<MyTableItem> selection;
  @Inject
  TableConfigurator<MyTableItem> configurator;
  private AtomicInteger onActionCounter;

  @Inject
  Navigator navigator;

  @Before
  public void setUp() throws Exception {
    TableView<MyTableItem> tableView = new TableView<>();
    configurator.addText(MyTableItem.class, MyTableItem::getName);

    Function<String, List<MyTableItem>> tableItemSupplier = this::getTableItems;
    StringConverter<MyTableItem> tableItemConverter = new StringConverter<MyTableItem>() {
      @Override
      public String toString(MyTableItem object) {
        return object.getName();
      }

      @Override
      public MyTableItem fromString(String string) {
        return tableItemSupplier.apply(string).get(0);
      }
    };
    selection.configure(tableView, this::getComboValue, tableItemSupplier, tableItemConverter);
    onActionCounter = new AtomicInteger();
    selection.setOnAction(e -> onActionCounter.incrementAndGet());
    FXPlatform.invokeLater(() -> navigator.present(selection.getRoot()));
  }

  @Test
  public void testSimpleTextEnter() throws Exception {
    FXPlatform.invokeLater(() -> selection.getTextField().setText("Item01"));
    if (selection.listPopup.isShowing()) {
      FXPlatform.invokeLater(() -> selection.listPopup.hide());
    }
    FXPlatform.invokeLater(() -> {
      KeyEvent event = new KeyEvent(null, "\n", "\n", KeyCode.ENTER, false, false, false, false);
      selection.getTextField().getOnKeyPressed().handle(event);
      log.info("Pressed ENTER");
    });
    FXPlatform.waitForFX();
    assertEquals(1, onActionCounter.get());
  }

  @Test
  public void testCompletionList() throws Exception {
    FXPlatform.invokeLater(() -> selection.textField.requestFocus());
    FXPlatform.invokeLater(() -> selection.getTextField().setText("Item"));
    FXPlatform.invokeLater(() -> selection.lastTextChange.trigger());
    FXPlatform.waitForFX();
    Condition.waitFor5s("Popup not showing yet", () -> selection.listPopup.isShowing());

    assertTrue(selection.listPopup.isShowing());
    assertEquals(20, selection.listView.getItems().size());

    FXPlatform.invokeLater(() -> selection.getTextField().setText("Item0"));
    FXPlatform.invokeLater(() -> selection.lastTextChange.trigger());
    FXPlatform.waitForFX();
    Condition.waitFor5s(() -> selection.listView.getItems().size(), Matchers.equalTo(10));

    assertEquals(0, onActionCounter.get());

    FXPlatform.invokeLater(() -> selection.listView.getSelectionModel().select(3));
    FXPlatform.invokeLater(() -> selection.listView.getOnMouseClicked().handle(null));
    FXPlatform.waitForFX();
    assertEquals(1, onActionCounter.get());
    assertEquals("Item03", selection.textField.getText());
  }

  @Test
  public void testTableSelection() throws Exception {
    FXPlatform.invokeLater(() -> selection.browse.getOnAction().handle(null));
    FXPlatform.waitForFX();

    assertTrue(selection.tablePopup.isShowing());
    assertEquals(20, selection.table.getItems().size());

    assertEquals(0, onActionCounter.get());
    FXPlatform.invokeLater(() -> selection.table.getSelectionModel().select(3));
    MouseEvent event = new ObjenesisStd().newInstance(MouseEvent.class);
    Field field = MouseEvent.class.getDeclaredField("clickCount");
    field.setAccessible(true);
    field.setInt(event, 2);

    FXPlatform.invokeLater(() -> selection.table.getOnMouseClicked().handle(event));
    FXPlatform.waitForFX();
    assertEquals(1, onActionCounter.get());
    assertEquals("Item03", selection.textField.getText());
  }

  private List<MyTableItem> getTableItems(String value) {
    ArrayList<MyTableItem> items = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      MyTableItem item = new MyTableItem().setName("Item" + String.format("%02d", i));
      if (item.getName().startsWith(value.trim())) {
        items.add(item);
      }
    }
    return items;
  }

  private List<String> getComboValue(String s) {
    return getTableItems(s).stream().filter(i -> i.getName().startsWith(s)).map(MyTableItem::getName).collect(Collectors.toList());
  }

}