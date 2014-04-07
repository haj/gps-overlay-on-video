package peregin.tov

import scala.swing._
import java.awt.{Point, Toolkit, Dimension}
import javax.imageio.ImageIO
import peregin.tov.gui.{DashboardPanel, VideoPanel, TelemetryPanel, MigPanel}
import javax.swing._
import com.jgoodies.looks.plastic.{PlasticTheme, PlasticLookAndFeel, Plastic3DLookAndFeel}
import org.jdesktop.swingx.{JXButton, JXLabel, JXStatusBar, JXTitledPanel}
import peregin.tov.util.{Timed, Logging}
import java.awt.event.{ActionEvent, ActionListener}
import peregin.tov.model.Setup
import javax.swing.filechooser.FileNameExtensionFilter


object App extends SimpleSwingApplication with Logging with Timed {

  log.info("initializing...")

  initLookAndFeel()

  val setup = Setup.empty
  val videoPanel = new VideoPanel(setup)
  val telemetryPanel = new TelemetryPanel(setup)

  val frame = new MainFrame {
    contents = new MigPanel("ins 5, fill", "[fill]", "[][fill]") {
      val toolbar = new JToolBar
      def createToolbarButton[T](image: String, tooltip: String, action: => T): JXButton = {
        val btn = new JXButton(loadIcon(image))
        btn.setToolTipText(tooltip)
        btn.addActionListener(new ActionListener {
          override def actionPerformed(e: ActionEvent) = action
        })
        btn
      }
      toolbar.add(createToolbarButton("images/new.png", "New", newProject))
      toolbar.add(createToolbarButton("images/open.png", "Open", openProject))
      toolbar.add(createToolbarButton("images/save.png", "Save", saveProject))
      toolbar.addSeparator()
      toolbar.add(createToolbarButton("images/play.png", "Export", exportProject))
      add(Component.wrap(toolbar), "span 2, wrap")

      add(titled("Video", videoPanel), "pushy, width 60%")
      add(titled("Telemetry Data", telemetryPanel), "pushy, width 40%, wrap")

      val dashboardPanel = new DashboardPanel
      add(titled("Dashboard", dashboardPanel), "height 30%, span 2, wrap")

      val statusPanel = new JXStatusBar
      statusPanel.add(new JXLabel("Ready"))
      add(Component.wrap(statusPanel), "span 2")
    }
  }

  frame.title = "Telemetry data on videos"
  frame.iconImage = loadImage("images/video.png")
  frame.size = new Dimension(1024, 768)
  center(frame)
  frame.maximize()

  def top = frame

  def initLookAndFeel() {
    import PlasticLookAndFeel._
    import collection.JavaConverters._
    val theme = getInstalledThemes.asScala.map(_.asInstanceOf[PlasticTheme]).find(_.getName == "Dark Star")
    theme.foreach(setPlasticTheme)
    UIManager.setLookAndFeel(new Plastic3DLookAndFeel())
  }

  def center(w: Window) {
    val screen = Toolkit.getDefaultToolkit.getScreenSize
    val size = w.bounds
    val x = (screen.width - size.width) / 2
    val y = (screen.height - size.height) / 2
    w.location = new Point(x, y)
  }

  def loadImage(path: String): Image = ImageIO.read(classOf[App].getClassLoader.getResourceAsStream(path))
  def loadIcon(path: String): Icon = new ImageIcon(loadImage(path))

  def titled(title: String, c: Component): Component = {
    val panel = new JXTitledPanel(title, c.peer)
    Component.wrap(panel)
  }
  
  def newProject() {
    log.info("new project")
    setup.reset()
    videoPanel.refreshFromSetup()
    telemetryPanel.refreshFromSetup()
  }

  def openProject(): Unit = timed("open project") {
    val chooser = new FileChooser()
    chooser.fileFilter = new FileNameExtensionFilter("project file (json)", "json")
    chooser.title = "Open project:"
    if (chooser.showOpenDialog(App.frame.contents.head) == FileChooser.Result.Approve) {
      val file = chooser.selectedFile
      log.debug(s"opening ${file.getAbsolutePath}")
      setup.copyAs(Setup.loadFile(file.getAbsolutePath))
      videoPanel.refreshFromSetup()
      telemetryPanel.refreshFromSetup()
    }
  }

  def saveProject() {
    val chooser = new FileChooser()
    chooser.fileFilter = new FileNameExtensionFilter("project file (json)", "json")
    chooser.title = "Save project:"
    if (chooser.showSaveDialog(App.frame.contents.head) == FileChooser.Result.Approve) {
      val file = chooser.selectedFile
      log.debug(s"saving ${file.getAbsolutePath}")
      setup.saveFile(file.getAbsolutePath)
    }
  }

  def exportProject() {
    log.info("export project")
  }
}
