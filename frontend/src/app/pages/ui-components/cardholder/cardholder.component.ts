import { Component, Input } from '@angular/core';

@Component({
  selector: 'custom-cardholder',
  templateUrl: './cardholder.component.html',
  styleUrl: './cardholder.component.scss'
})
export class CardholderComponent {
  @Input('flip') flip = false;

  _no: string = "---- ---- ---- ----";
  _cvc: string = "---";

  @Input('no') set no(value:any) {
    if (value) {
      this._no = this.formatCreditCardNumber(value);
    }
  }

  @Input('cvc') set cvc(val:any) {
    if (val) this._cvc = val;
  }

  @Input('holder') holder: string = "----- ----"
  @Input('month') month: string =  "--";
  @Input('year') year: string = "--";

  formatCreditCardNumber(value:any) {
    var v = value.replace(/\s+/g, '').replace(/([^0-9])/gi, '')
    var matches = v.match(/\d{4,16}/g);
    var match = matches && matches[0] || ''
    var parts = []
    for (var i=0, len=match.length; i<len; i+=4) {
      parts.push(match.substring(i, i+4))
    }
    if (parts.length) {
      return parts.join(' ')
    } else {
      return value
    }
  }
}
