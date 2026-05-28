import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UnlockRequestComponent } from './unlock-request-component';

describe('UnlockRequestComponent', () => {
  let component: UnlockRequestComponent;
  let fixture: ComponentFixture<UnlockRequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UnlockRequestComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(UnlockRequestComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
