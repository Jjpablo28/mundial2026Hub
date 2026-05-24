import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TradeScreen } from './trade-screen';

describe('TradeScreen', () => {
  let component: TradeScreen;
  let fixture: ComponentFixture<TradeScreen>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TradeScreen],
    }).compileComponents();

    fixture = TestBed.createComponent(TradeScreen);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
