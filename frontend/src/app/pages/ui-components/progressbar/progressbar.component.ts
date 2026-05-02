import { Component, Input } from '@angular/core';
import { ProgressBarMode } from '@angular/material/progress-bar';

@Component({
  selector: 'custom-progressbar',
  templateUrl: './progressbar.component.html',
  styleUrl: './progressbar.component.scss'
})
export class ProgressbarComponent {
 /** Input to configure the mode of the progress bar */
 @Input() progressMode: ProgressBarMode = 'determinate';

 /* add background color with header title */
 @Input() withBackground: boolean = false;

 
  /** Input to configure the mode of the progress bar */
  @Input() label:String ='';

 /** Input to configure the value for 'determinate' or 'buffer' mode */
 @Input() progressValue: number = 0;

 /** Input to configure the color of the progress bar */
 @Input() color: 'primary' | 'accent' | 'warn' = 'primary';

 /** Available modes for the progress bar */
 readonly availableModes: ProgressBarMode[] = ['determinate', 'indeterminate', 'buffer', 'query'];
}
