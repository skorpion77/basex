package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import static org.basex.query.xquery.XQTokens.*;

import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.FAttr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.QNm;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.item.Uri;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import org.basex.util.XMLToken;

/**
 * Attribute fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class CAttr extends Arr {
  /** Tag name. */
  private Expr atn;
  /** Computed constructor. */
  private final boolean comp;

  /**
   * Constructor.
   * @param n name
   * @param v attribute values
   * @param c computed construction flag
   */
  public CAttr(final Expr n, final Expr[] v, final boolean c) {
    super(v);
    atn = n;
    comp = c;
  }

  @Override
  public Expr comp(final XQContext ctx) throws XQException {
    super.comp(ctx);
    atn = atn.comp(ctx);
    return this;
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final QNm name = name(ctx, ctx.atomic(atn, this, false));
    final byte[] pre = name.pre();
    final byte[] ln = name.ln();
    if(comp && (Token.eq(name.str(), XMLNS) || Token.eq(pre, XMLNS)))
      Err.or(NSATTCONS);

    final TokenBuilder tb = new TokenBuilder();
    for(final Expr e : expr) CText.add(tb, ctx.iter(e));
    byte[] val = tb.finish();
    if(Token.eq(pre, XML) && Token.eq(ln, ID)) val = Token.norm(val);

    return new FAttr(name, val, null).iter();
  }

  /**
   * Returns an updated name expression.
   * @param ctx query context
   * @param it item
   * @return result
   * @throws XQException query exception
   */
  public static QNm name(final XQContext ctx, final Item it)
      throws XQException {

    QNm name = null;
    if(it.type == Type.QNM) {
      name = (QNm) it;
    } else {
      final byte[] nm = it.str();
      if(Token.contains(nm, ' ')) Err.or(INVAL, nm);
      if(!XMLToken.isQName(nm)) Err.or(NAMEWRONG, nm);
      name = new QNm(nm);
    }
    if(name.ns() && name.uri == Uri.EMPTY) name.uri = ctx.ns.uri(name.pre());
    return name;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    ser.openElement(NAME);
    atn.plan(ser);
    ser.closeElement(NAME);
    ser.openElement(VALUE);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement(VALUE);
    ser.closeElement(this);
  }

  @Override
  public String color() {
    return "FF3333";
  }

  @Override
  public String info() {
    return "Attribute constructor";
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("attribute " + atn + " { ");
    sb.append(toString(", "));
    return sb.append(" }").toString();
  }
}
