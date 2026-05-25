import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-travel-placeholder',
  templateUrl: './travel-placeholder.component.html',
  styleUrl: './travel-placeholder.component.scss',
})
export class TravelPlaceholderComponent {
  title = 'Coming soon';

  constructor(private readonly route: ActivatedRoute) {
    this.title = (route.snapshot.data['title'] as string) ?? 'Coming soon';
  }
}
