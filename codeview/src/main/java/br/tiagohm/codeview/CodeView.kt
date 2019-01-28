package br.tiagohm.codeview

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import java.util.*
import java.util.regex.Pattern

class CodeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : NestedWebView(context, attrs, defStyleAttr) {

  private var code = ""
  private var escapeCode: String? = null
  private var theme: Theme? = null
  private var language: Language? = null
  private var fontSize = 16f
  private var wrapLine = false
  private var onHighlightListener: OnHighlightListener? = null
  private var pinchDetector: ScaleGestureDetector? = null
  private var zoomEnabled = false
  private var showLineNumber = false
  private var startLineNumber = 1
  /**
   * Obtém a quantidade de linhas no código.
   */
  var lineCount = 0
    private set
  var highlightLineNumber = -1
    private set

  interface OnHighlightListener {
    fun onStartCodeHighlight()

    fun onFinishCodeHighlight()

    fun onLanguageDetected(language: Language, relevance: Int)

    fun onFontSizeChanged(sizeInPx: Int)

    fun onLineClicked(lineNumber: Int, content: String)
  }

  init {
    //Inicialização.
    init(context, attrs)
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (isZoomEnabled()) {
      pinchDetector!!.onTouchEvent(event)
    }
    return super.onTouchEvent(event)
  }

  private fun init(context: Context, attrs: AttributeSet?) {
    val attributes = context.theme.obtainStyledAttributes(
      attrs,
      R.styleable.CodeView, 0, 0
    )
    //Define os atributos
    setWrapLine(attributes.getBoolean(R.styleable.CodeView_cv_wrap_line, false))
    setFontSize(attributes.getInt(R.styleable.CodeView_cv_font_size, 14).toFloat())
    setZoomEnabled(attributes.getBoolean(R.styleable.CodeView_cv_zoom_enable, false))
    setShowLineNumber(attributes.getBoolean(R.styleable.CodeView_cv_show_line_number, false))
    setStartLineNumber(attributes.getInt(R.styleable.CodeView_cv_start_line_number, 1))
    highlightLineNumber = attributes.getInt(R.styleable.CodeView_cv_highlight_line_number, -1)
    attributes.recycle()

    pinchDetector = ScaleGestureDetector(context, PinchListener())

    webChromeClient = WebChromeClient()
    settings.javaScriptEnabled = true
    settings.cacheMode = WebSettings.LOAD_NO_CACHE
    settings.loadWithOverviewMode = true

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      WebView.setWebContentsDebuggingEnabled(true)
    }
  }

  /**
   * Define um listener.
   */
  fun setOnHighlightListener(listener: OnHighlightListener?): CodeView {
    //Definir um listener.
    if (listener != null) {
      //Definir um novo listener
      if (onHighlightListener !== listener) {
        onHighlightListener = listener
        //Adiciona o objeto que atenderá os eventos js e disparará o listener definido.
        addJavascriptInterface(object : Any() {
          @JavascriptInterface
          fun onStartCodeHighlight() {
            if (onHighlightListener != null) {
              onHighlightListener!!.onStartCodeHighlight()
            }
          }

          @JavascriptInterface
          fun onFinishCodeHighlight() {
            if (onHighlightListener != null) {
              Handler(Looper.getMainLooper()).post {
                fillLineNumbers()
                showHideLineNumber(isShowLineNumber())
                highlightLineNumber(highlightLineNumber)
              }
              onHighlightListener!!.onFinishCodeHighlight()
            }
          }

          @JavascriptInterface
          fun onLanguageDetected(name: String, relevance: Int) {
            if (onHighlightListener != null) {
              onHighlightListener!!.onLanguageDetected(Language.getLanguageByName(name), relevance)
            }
          }

          @JavascriptInterface
          fun onLineClicked(lineNumber: Int, content: String) {
            if (onHighlightListener != null) {
              onHighlightListener!!.onLineClicked(lineNumber, content)
            }
          }
        }, "android")
      }
    } else {
      removeJavascriptInterface("android")
    }//Remover o listener.
    return this
  }

  /**
   * Obtém o tamanho da fonte do texto em pixels.
   */
  fun getFontSize(): Float {
    return fontSize
  }

  /**
   * Define o tamanho da fonte do texto em pixels.
   */
  fun setFontSize(fontSize: Float): CodeView {
    var fontSize = fontSize
    if (fontSize < 8) fontSize = 8f
    this.fontSize = fontSize
    if (onHighlightListener != null) {
      onHighlightListener!!.onFontSizeChanged(fontSize.toInt())
    }
    return this
  }

  /**
   * Obtém o código exibido.
   */
  fun getCode(): String {
    return code
  }

  /**
   * Define o código que será exibido.
   */
  fun setCode(code: String?): CodeView {
    var code = code
    if (code == null) code = ""
    this.code = code
    this.escapeCode = Html.escapeHtml(code)
    return this
  }

  /**
   * Obtém o tema.
   */
  fun getTheme(): Theme? {
    return theme
  }

  /**
   * Define o tema.
   */
  fun setTheme(theme: Theme): CodeView {
    this.theme = theme
    return this
  }

  /**
   * Obtém a linguagem.
   */
  fun getLanguage(): Language? {
    return language
  }

  /**
   * Define a linguagem.
   */
  fun setLanguage(language: Language): CodeView {
    this.language = language
    return this
  }

  /**
   * Verifica se está aplicando a quebra de linha.
   */
  fun isWrapLine(): Boolean {
    return wrapLine
  }

  /**
   * Define se aplicará a quebra de linha.
   */
  fun setWrapLine(wrapLine: Boolean): CodeView {
    this.wrapLine = wrapLine
    return this
  }

  /**
   * Verifica se o zoom está habilitado.
   */
  fun isZoomEnabled(): Boolean {
    return zoomEnabled
  }

  /**
   * Define que o zoom estará habilitado ou não.
   */
  fun setZoomEnabled(zoomEnabled: Boolean): CodeView {
    this.zoomEnabled = zoomEnabled
    return this
  }

  /**
   * Verifica se o número da linha está sendo exibido.
   */
  fun isShowLineNumber(): Boolean {
    return showLineNumber
  }

  /**
   * Define a visibilidade do número da linha.
   */
  fun setShowLineNumber(showLineNumber: Boolean): CodeView {
    this.showLineNumber = showLineNumber
    return this
  }

  /**
   * Obtém o número da primeira linha.
   */
  fun getStartLineNumber(): Int {
    return startLineNumber
  }

  /**
   * Define o número da primeira linha.
   */
  fun setStartLineNumber(startLineNumber: Int): CodeView {
    var startLineNumber = startLineNumber
    if (startLineNumber < 0) startLineNumber = 1
    this.startLineNumber = startLineNumber
    return this
  }

  /**
   * Exibe ou oculta o número da linha.
   */
  fun toggleLineNumber() {
    showLineNumber = !showLineNumber
    showHideLineNumber(showLineNumber)
  }

  /**
   * Aplica os atributos e exibe o código.
   */
  fun apply() {
    loadDataWithBaseURL(
      "",
      toHtml(),
      "text/html",
      "UTF-8",
      ""
    )
  }

  private fun toHtml(): String {
    val sb = StringBuilder()
    //html
    sb.append("<!DOCTYPE html>\n")
      .append("<html>\n")
      .append("<head>\n")
    //style
    sb.append("<link rel='stylesheet' href='").append(getTheme()!!.path).append("' />\n")
    sb.append("<style>\n")
    //body
    sb.append("body {")
    sb.append("font-size:").append(String.format("%dpx;", getFontSize().toInt()))
    sb.append("margin: 0px; line-height: 1.2;")
    sb.append("}\n")
    //.hljs
    sb.append(".hljs {")
    sb.append("}\n")
    //pre
    sb.append("pre {")
    sb.append("margin: 0px; position: relative;")
    sb.append("}\n")
    //line
    if (isWrapLine()) {
      sb.append("td.line {")
      sb.append("word-wrap: break-word; white-space: pre-wrap; word-break: break-all;")
      sb.append("}\n")
    }
    //Outros
    sb.append("table, td, tr {")
    sb.append("margin: 0px; padding: 0px;")
    sb.append("}\n")
    sb.append("code > span { display: none; }")
    sb.append("td.ln { text-align: right; padding-right: 2px; }")
    sb.append("td.line:hover span {background: #661d76; color: #fff;}")
    sb.append("td.line:hover {background: #661d76; color: #fff; border-radius: 2px;}")
    sb.append("td.destacado {background: #ffda11; color: #000; border-radius: 2px;}")
    sb.append("td.destacado span {background: #ffda11; color: #000;}")
    sb.append("</style>")
    //scripts
    sb.append("<script src='file:///android_asset/highlightjs/highlight.js'></script>")
    sb.append("<script>hljs.initHighlightingOnLoad();</script>")
    sb.append("</head>")
    //code
    sb.append("<body>")
    sb.append("<pre><code class='").append(language!!.languageName).append("'>")
      .append(insertLineNumber(escapeCode))
      .append("</code></pre>\n")
    return sb.toString()
  }

  private fun executeJavaScript(js: String) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
      evaluateJavascript("javascript:$js", null)
    } else {
      loadUrl("javascript:$js")
    }
  }

  private fun changeFontSize(sizeInPx: Int) {
    executeJavaScript("document.body.style.fontSize = '" + sizeInPx + "px'")
  }

  private fun fillLineNumbers() {
    executeJavaScript("var i; var x = document.querySelectorAll('td.ln'); for(i = 0; i < x.length; i++) {x[i].innerHTML = x[i].getAttribute('line');}")
  }

  private fun showHideLineNumber(show: Boolean) {
    executeJavaScript(
      String.format(
        Locale.ENGLISH,
        "var i; var x = document.querySelectorAll('td.ln'); for(i = 0; i < x.length; i++) {x[i].style.display = %s;}",
        if (show) "''" else "'none'"
      )
    )
  }

  fun highlightLineNumber(lineNumber: Int) {
    this.highlightLineNumber = lineNumber
    executeJavaScript(
      String.format(
        Locale.ENGLISH,
        "var x = document.querySelectorAll('.destacado'); if(x && x.length == 1) x[0].classList.remove('destacado');"
      )
    )
    if (lineNumber >= 0) {
      executeJavaScript(
        String.format(
          Locale.ENGLISH,
          "var x = document.querySelectorAll(\"td.line[line='%d']\"); if(x && x.length == 1) x[0].classList.add('destacado');", lineNumber
        )
      )
    }
  }

  private fun insertLineNumber(code: String?): String {
    val m = Pattern.compile("(.*?)&#10;").matcher(code)
    val sb = StringBuffer()
    //Posição atual da linha.
    var pos = getStartLineNumber()
    //Quantidade de linhas.
    lineCount = 0
    //Para cada linha encontrada, encapsulá-la dentro uma linha de uma tabela.
    while (m.find()) {
      m.appendReplacement(
        sb,
        String.format(
          Locale.ENGLISH,
          "<tr><td line='%d' class='hljs-number ln'></td><td line='%d' onclick='android.onLineClicked(%d, this.textContent);' class='line'>$1 </td></tr>&#10;",
          pos, pos, pos
        )
      )
      pos++
      lineCount++
    }

    return "<table>\n" + sb.toString().trim { it <= ' ' } + "</table>\n"
  }

  /**
   * Eventos de pinça.
   */
  private inner class PinchListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

    private var fontSize: Float = 0.toFloat()
    private var oldFontSize: Int = 0

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
      fontSize = getFontSize()
      oldFontSize = fontSize.toInt()
      return super.onScaleBegin(detector)
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
      this@CodeView.fontSize = fontSize
      super.onScaleEnd(detector)
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
      fontSize = getFontSize() * detector.scaleFactor
      if (fontSize >= 8) {
        changeFontSize(fontSize.toInt())
        if (onHighlightListener != null && oldFontSize != fontSize.toInt()) {
          onHighlightListener!!.onFontSizeChanged(fontSize.toInt())
        }
        oldFontSize = fontSize.toInt()
      } else {
        fontSize = 8f
      }
      return false
    }
  }
}
